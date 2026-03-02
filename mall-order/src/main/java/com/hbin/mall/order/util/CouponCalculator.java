package com.hbin.mall.order.util;

import com.hbin.mall.order.domain.CouponTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CouponCalculator {
    
    public BigDecimal calculateDiscount(CouponTemplate template, BigDecimal orderAmount) {
        if (template.getCouponType() == 1) { // 满减
            return template.getDiscountAmount();
        } else if (template.getCouponType() == 2) { // 折扣
            BigDecimal discount = orderAmount.multiply(template.getDiscountAmount()).divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
            return orderAmount.subtract(discount);
        } else if (template.getCouponType() == 3) { // 无门槛
            return template.getDiscountAmount();
        }
        return BigDecimal.ZERO;
    }
    
    public boolean isValid(CouponTemplate template, BigDecimal orderAmount) {
        if (template.getMinOrderAmount() != null) {
            return orderAmount.compareTo(template.getMinOrderAmount()) >= 0;
        }
        return true;
    }
}