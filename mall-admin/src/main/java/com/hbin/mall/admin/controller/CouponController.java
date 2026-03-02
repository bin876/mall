package com.hbin.mall.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.api.order.feign.CouponFeignClient;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class CouponController {

    @Autowired
    private CouponFeignClient couponFeignClient;

    @PostMapping("/templates")
    public Result<String> createCouponTemplate(
            @Valid @RequestBody CouponTemplateCreateRequest request) {
        return couponFeignClient.createCouponTemplate(request);
    }

    /**
     * 更新优惠券模板
     */
    @PutMapping("/templates/{templateId}")
    public Result<String> updateCouponTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody CouponTemplateUpdateRequest request) {
        return couponFeignClient.updateCouponTemplate(templateId, request);
    }

    /**
     * 删除优惠券模板
     */
    @DeleteMapping("/templates/{templateId}")
    public Result<String> deleteCouponTemplate(@PathVariable Long templateId) {
        return couponFeignClient.deleteCouponTemplate(templateId);
    }

    /**
     * 获取优惠券模板列表
     */
    @PostMapping("/templateList")
    public Result<Page<CouponTemplateDTO>> getCouponTemplates(
            @RequestBody CouponTemplateSearchRequest request) {
        return couponFeignClient.getCouponTemplates(request);
    }

    /**
     * 获取单个优惠券模板详情
     */
    @GetMapping("/templates/{templateId}")
    public Result<CouponTemplateDTO> getCouponTemplate(@PathVariable Long templateId) {
        return couponFeignClient.getCouponTemplate(templateId);
    }

    /**
     * 更新优惠券模板状态
     */
    @PutMapping("/templates/{templateId}/status")
    public Result<String> updateTemplateStatus(
            @PathVariable Long templateId,
            @RequestParam Integer status) {
        return couponFeignClient.updateTemplateStatus(templateId, status);
    }

    /**
     * 发放优惠券
     */
    @PostMapping("/issue")
    public Result<String> issueCoupons(@Valid @RequestBody CouponIssueRequest request) {
        return couponFeignClient.issueCoupons(request);
    }

    /**
     * 更新用户优惠券状态
     */
    @PutMapping("/user-coupons/status")
    public Result<String> updateUserCouponStatus(@Valid @RequestBody CouponUpdateStatusRequest request) {
        return couponFeignClient.updateUserCouponStatus(request);
    }

    /**
     * 获取用户优惠券列表
     */
    @PostMapping("/user-coupons")
    public Result<Page<UserCouponDTO>> getUserCoupons(
            @RequestBody UserCouponsRequest request) {
        return couponFeignClient.getUserCoupons(request);
    }

    /**
     * 获取单个用户优惠券详情
     */
    @GetMapping("/user-coupons/{couponId}")
    public Result<UserCouponDTO> getUserCoupon(@PathVariable Long couponId) {
        return couponFeignClient.getUserCoupon(couponId);
    }

    /**
     * 统计用户优惠券
     */
    @GetMapping("/user-coupons/{memberId}/count")
    public Result<Map<String, Object>> countUserCoupons(@PathVariable Long memberId) {
        return couponFeignClient.countUserCoupons(memberId);
    }

    /**
     * 清理过期优惠券
     */
    @PostMapping("/clean-expired")
    public Result<Integer> cleanExpiredCoupons() {
        return couponFeignClient.cleanExpiredCoupons();
    }
}