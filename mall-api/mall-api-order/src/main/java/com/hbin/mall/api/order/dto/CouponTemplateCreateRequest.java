package com.hbin.mall.api.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponTemplateCreateRequest {
    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 100, message = "优惠券名称不能超过100个字符")
    private String name;
    
    @Size(max = 200, message = "描述不能超过200个字符")
    private String description;
    
    @NotNull(message = "优惠券类型不能为空")
    @Min(value = 1, message = "优惠券类型不合法")
    @Max(value = 3, message = "优惠券类型不合法")
    private Integer couponType; // 1-满减, 2-折扣, 3-无门槛
    
    @NotNull(message = "优惠金额/折扣比例不能为空")
    @Positive(message = "优惠金额/折扣比例必须大于0")
    private BigDecimal discountAmount; // 满减金额/折扣比例
    
    private BigDecimal minOrderAmount; // 最低订单金额
    
    private Integer totalCount = -1; // 总发行量(-1=不限)
    
    @NotNull(message = "每人限领不能为空")
    @Min(value = 1, message = "每人限领必须大于0")
    private Integer perUserLimit = 1; // 每人限领
    
    private Integer validDays; // 领取后有效天数
    
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime; // 有效期开始
    
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime; // 有效期结束
    
    private Integer status = 1; // 0-禁用, 1-启用
}
