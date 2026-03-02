// CanalSyncService.java（完整版）
package com.hbin.mall.product.service;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.hbin.mall.product.domain.Brand;
import com.hbin.mall.product.domain.Category;
import com.hbin.mall.product.domain.SpuDocument;
import com.hbin.mall.product.mapper.BrandMapper;
import com.hbin.mall.product.mapper.CategoryMapper;
import com.hbin.mall.product.util.CategoryPathUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class CanalSyncService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private BrandMapper brandMapper;

    private static final String CANAL_SERVER_HOST = "192.168.72.130";
    private static final int CANAL_SERVER_PORT = 11111;
    private static final String CANAL_DESTINATION = "example";

    private final ConcurrentHashMap<Long, String> brandCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void startSync() {
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(CANAL_SERVER_HOST, CANAL_SERVER_PORT),
                CANAL_DESTINATION,
                "",
                ""
        );

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                connector.connect();
                connector.subscribe("mall\\.spu");

                while (true) {
                    try {
                        Message message = connector.getWithoutAck(100);
                        long batchId = message.getId();
                        int size = message.getEntries().size();

                        if (batchId == -1 || size == 0) {
                            Thread.sleep(1000);
                            continue;
                        }

                        boolean success = true;

                        for (Entry entry : message.getEntries()) {
                            if (entry.getEntryType() != EntryType.ROWDATA) {
                                continue;
                            }

                            try {
                                RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                                EventType eventType = rowChange.getEventType();
                                String tableName = entry.getHeader().getTableName();

                                if ("spu".equals(tableName)) {
                                    handleSpuData(eventType, rowChange.getRowDatasList());
                                }

                                for (RowData rowData : rowChange.getRowDatasList()) {
                                    if (eventType == EventType.DELETE) {
                                        log.debug("表 {} 删除: {}", tableName, rowToString(rowData.getBeforeColumnsList()));
                                    } else {
                                        log.debug("表 {} 新增/更新: {}", tableName, rowToString(rowData.getAfterColumnsList()));
                                    }
                                }

                            } catch (Exception e) {
                                log.error("处理单条记录失败", e);
                                success = false;
                            }
                        }

                        if (success) {
                            connector.ack(batchId);
                        } else {
                            connector.rollback(batchId);
                        }

                    } catch (Exception e) {
                        log.error("Canal 同步处理错误", e);
                        try { Thread.sleep(1000); } catch (InterruptedException ex) { break; }
                    }
                }

            } catch (Exception e) {
                log.error("Canal 客户端连接失败", e);
            } finally {
                connector.disconnect();
            }
        });
    }

    private void handleSpuData(EventType eventType, List<RowData> rowDatas) {
        for (RowData rowData : rowDatas) {
            try {
                if (eventType == EventType.DELETE) {
                    String spuId = getRowValue(rowData.getBeforeColumnsList(), "spu_id");
                    if (spuId != null) {
                        elasticsearchOperations.delete(spuId, SpuDocument.class);
                    }
                } else {
                    SpuDocument doc = buildSpuDocument(rowData.getAfterColumnsList());
                    if (doc != null && doc.getSpuId() != null) {
                        elasticsearchOperations.save(doc);
                    }
                }
            } catch (Exception e) {
                log.error("ES 同步失败", e);
            }
        }
    }

    private SpuDocument buildSpuDocument(List<Column> columns) {
        SpuDocument doc = new SpuDocument();
        Long brandId = null;
        Long categoryId = null;

        for (Column col : columns) {
            switch (col.getName()) {
                case "spu_id":
                    doc.setSpuId(Long.valueOf(col.getValue()));
                    break;
                case "name":
                    doc.setName(col.getValue());
                    break;
                case "min_price":
                    doc.setMinPrice(Double.valueOf(col.getValue()));
                    break;
                case "max_price":
                    doc.setMaxPrice(Double.valueOf(col.getValue()));
                    break;
                case "sale_count":
                    doc.setSaleCount(Integer.valueOf(col.getValue()));
                    break;
                case "default_img":
                    doc.setDefaultImg(col.getValue());
                    break;
                case "create_time":
                    String timeStr = col.getValue();
                    if (timeStr != null && !timeStr.trim().isEmpty()) {
                        LocalDateTime localDateTime = parseCreateTime(timeStr.trim());
                        if (localDateTime != null) {
                            // 转换为时间戳
                            long timestamp = localDateTime.atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli();
                            doc.setCreateTime(timestamp);
                        }
                    }
                    break;
                case "brand_id":
                    brandId = Long.valueOf(col.getValue());
                    doc.setBrandId(brandId);
                    break;
                case "category_id":
                    categoryId = Long.valueOf(col.getValue());
                    doc.setCategoryId(categoryId);
                    break;
            }
        }

        if (brandId != null) {
            String brandName = brandCache.computeIfAbsent(brandId, id -> {
                Brand brand = brandMapper.selectById(id);
                return brand != null ? brand.getName() : "未知品牌";
            });
            doc.setBrandName(brandName);
        }

        return doc;
    }

    private String getRowValue(List<Column> columns, String columnName) {
        for (Column col : columns) {
            if (columnName.equals(col.getName())) {
                return col.getValue();
            }
        }
        return null;
    }

    private String rowToString(List<Column> columns) {
        StringBuilder sb = new StringBuilder();
        for (Column col : columns) {
            sb.append(col.getName()).append("=").append(col.getValue()).append(", ");
        }
        return sb.toString();
    }

    private static final List<DateTimeFormatter> TIME_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
    );

    private LocalDateTime parseCreateTime(String timeStr) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                TemporalAccessor temporal = formatter.parse(timeStr);
                if (temporal instanceof LocalDate) {
                    return ((LocalDate) temporal).atStartOfDay();
                } else {
                    return LocalDateTime.from(temporal);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}