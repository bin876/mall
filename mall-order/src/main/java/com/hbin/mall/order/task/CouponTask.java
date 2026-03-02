package com.hbin.mall.order.task;

import com.hbin.mall.order.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponTask {

    @Autowired
    private UserCouponService userCouponService;

    /**
     * 每天凌晨2点清理过期优惠券
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredCoupons() {
        int cleanedCount = userCouponService.cleanExpiredCoupons(LocalDateTime.now());
        log.info("清理过期优惠券 {} 张", cleanedCount);
    }
}