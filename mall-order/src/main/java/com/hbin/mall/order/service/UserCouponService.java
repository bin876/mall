package com.hbin.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.order.domain.UserCoupon;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

public interface UserCouponService extends IService<UserCoupon> {

    @Transactional
    void receiveCoupon(Long memberId, Long templateId);

    @Transactional(rollbackFor = Exception.class)
    void issueCouponsToUsers(CouponIssueRequest request);

    @Transactional(rollbackFor = Exception.class)
    void updateUserCouponStatus(CouponUpdateStatusRequest request);

    Page<UserCouponDTO> getUserCoupons(PageRequest pageRequest, UserCouponQueryParams params);

    UserCouponDTO getUserCoupon(Long couponId);

    Map<String, Object> countUserCoupons(Long memberId);

    @Transactional(rollbackFor = Exception.class)
    int cleanExpiredCoupons(LocalDateTime currentTime);
}
