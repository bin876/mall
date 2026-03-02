package com.hbin.mall.api.seckill.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityEditDTO {
    private Long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Byte status; // 0-未开始,1-进行中,2-已结束
    
    // 编辑专用字段
    private List<SeckillSkuEditDTO> skus;
    private Boolean canEdit;           // 是否允许编辑
    private Boolean canAddSkus;        // 是否允许添加SKU
    private Boolean canRemoveSkus;     // 是否允许移除SKU
    private String editLimitMessage;   // 编辑限制提示
    
    public String getStatusText() {
        switch (status) {
            case 0: return "未开始";
            case 1: return "进行中";
            case 2: return "已结束";
            default: return "未知状态";
        }
    }
    
    public String getTimeRange() {
        return startTime != null ? 
               startTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + 
               " 至 " + 
               (endTime != null ? endTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "未设置") 
               : "时间未设置";
    }
}