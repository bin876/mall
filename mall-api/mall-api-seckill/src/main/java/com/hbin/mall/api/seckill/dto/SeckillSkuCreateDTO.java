package com.hbin.mall.api.seckill.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuCreateDTO {
    @NotNull(message = "SPU ID不能为空")
    private Long spuId;
    
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;
    
    @NotNull(message = "秒杀价格不能为空")
    @Min(value = 0, message = "秒杀价格必须大于等于0")
    @Digits(integer = 10, fraction = 2, message = "秒杀价格格式错误")
    private BigDecimal seckillPrice;
    
    @NotNull(message = "库存数量不能为空")
    @Min(value = 1, message = "库存数量必须大于0")
    private Integer stock;
}