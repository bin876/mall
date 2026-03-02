package com.hbin.mall.api.order.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "mall-order", contextId = "coupon")
public interface CouponFeignClient {

    // @PostMapping("/templates")
    // Result<String> createCouponTemplate(@Valid @RequestBody CouponTemplateCreateRequest request);

    // @Override
    // @PostMapping("/templates")
    // public Result<String> createCouponTemplate(@Valid @RequestBody CouponTemplateCreateRequest request) {
    //     couponTemplateService.createCouponTemplate(request);
    //     return Result.success("优惠券模板创建成功");
    // }

    /**
     * 创建优惠券模板
     */
    @PostMapping("/admin/coupons/templates")
    Result<String> createCouponTemplate(@RequestBody CouponTemplateCreateRequest request);

    /**
     * 更新优惠券模板
     */
    @PutMapping("/admin/coupons/templates/{templateId}")
    Result<String> updateCouponTemplate(
            @PathVariable Long templateId,
            @RequestBody CouponTemplateUpdateRequest request);

    @DeleteMapping("/templates/{templateId}")
    Result<String> deleteCouponTemplate(@PathVariable Long templateId);

    @PostMapping("/templateList")
    Result<Page<CouponTemplateDTO>> getCouponTemplates(
            @RequestBody CouponTemplateSearchRequest request);

    @GetMapping("/templates/{templateId}")
    Result<CouponTemplateDTO> getCouponTemplate(@PathVariable Long templateId);

    @PutMapping("/templates/{templateId}/status")
    Result<String> updateTemplateStatus(
            @PathVariable Long templateId,
            @RequestParam Integer status);

    @PostMapping("/issue")
    Result<String> issueCoupons(@Valid @RequestBody CouponIssueRequest request);

    @PutMapping("/user-coupons/status")
    Result<String> updateUserCouponStatus(@Valid @RequestBody CouponUpdateStatusRequest request);

    @PostMapping("/user-coupons")
    Result<Page<UserCouponDTO>> getUserCoupons(
            @RequestBody UserCouponsRequest request);

    @GetMapping("/user-coupons/{couponId}")
    Result<UserCouponDTO> getUserCoupon(@PathVariable Long couponId);

    @GetMapping("/user-coupons/{memberId}/count")
    Result<Map<String, Object>> countUserCoupons(@PathVariable Long memberId);

    @PostMapping("/clean-expired")
    Result<Integer> cleanExpiredCoupons();
}
