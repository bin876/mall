package com.hbin.mall.api.product.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BatchUpdateSkuDTO {
    @NotNull(message = "SKU ID列表不能为空")
    @Size(min = 1, message = "至少需要选择一个SKU")
    private List<Long> skuIds;
    
    private BigDecimal price;
    private String priceOperation; // set, add, reduce
    
    private Integer stock;
    private String stockOperation; // set, add, reduce
    
    private Integer status;
}