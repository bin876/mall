package com.hbin.mall.product.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "spu", createIndex = true)
public class SpuDocument {
    
    @Id
    private Long spuId;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Double)
    private Double minPrice;
    @Field(type = FieldType.Double)
    private Double maxPrice;
    @Field(type = FieldType.Integer)
    private Integer saleCount;
    @Field(type = FieldType.Keyword)
    private String defaultImg;
    // 时间戳
    @Field(type = FieldType.Long)
    private Long createTime;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Long)
    private Long brandId;
    @Field(type = FieldType.Keyword)
    private String brandName;
}