package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.api.order.feign.CouponFeignClient;
import com.hbin.mall.common.exception.BizException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.service.CouponTemplateService;
import com.hbin.mall.order.service.UserCouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class AdminCouponController implements CouponFeignClient {

    @Autowired
    private CouponTemplateService couponTemplateService;

    @Autowired
    private UserCouponService userCouponService;

    /**
     * 创建优惠券模板
     */
    @Override
    public Result<String> createCouponTemplate(@Valid @RequestBody CouponTemplateCreateRequest request) {
        try {
            couponTemplateService.createCouponTemplate(request);
            return Result.success("优惠券模板创建成功");
        } catch (Exception e) {
            // 修复：捕获并处理验证异常
            if (e instanceof BizException) {
                return Result.error(e.getMessage());
            } else {
                return Result.error("创建优惠券模板失败");
            }
        }
    }

    /**
     * 更新优惠券模板
     * 修复：移除BindingResult参数
     */
    @Override
    public Result<String> updateCouponTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody CouponTemplateUpdateRequest request) {
        try {
            request.setTemplateId(templateId);
            couponTemplateService.updateCouponTemplate(request);
            return Result.success("优惠券模板更新成功");
        } catch (Exception e) {
            // 修复：捕获并处理验证异常
            if (e instanceof BizException) {
                return Result.error(e.getMessage());
            } else {
                return Result.error("更新优惠券模板失败");
            }
        }
    }

    /**
     * 删除优惠券模板
     */
    @Override
    @DeleteMapping("/templates/{templateId}")
    public Result<String> deleteCouponTemplate(@PathVariable Long templateId) {
        couponTemplateService.deleteCouponTemplate(templateId);
        return Result.success("优惠券模板删除成功");
    }

    /**
     * 获取优惠券模板列表
     */
    @Override
    public Result<Page<CouponTemplateDTO>> getCouponTemplates(
            @RequestBody CouponTemplateSearchRequest request) {

        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(request.getPageNum());
        pageRequest.setPageSize(request.getPageSize());

        Page<CouponTemplateDTO> page = couponTemplateService.getCouponTemplates(pageRequest, request.getParams());
        return Result.success(page);
    }

    /**
     * 获取单个优惠券模板详情
     */
    @Override
    public Result<CouponTemplateDTO> getCouponTemplate(@PathVariable Long templateId) {
        CouponTemplateDTO dto = couponTemplateService.getCouponTemplate(templateId);
        return Result.success(dto);
    }

    /**
     * 更新优惠券模板状态
     */
    @Override
    public Result<String> updateTemplateStatus(
            @PathVariable Long templateId,
            @RequestParam Integer status) {
        couponTemplateService.updateTemplateStatus(templateId, status);
        return Result.success("优惠券模板状态更新成功");
    }

    /**
     * 发放优惠券
     */
    @Override
    public Result<String> issueCoupons(@Valid @RequestBody CouponIssueRequest request) {
        userCouponService.issueCouponsToUsers(request);
        return Result.success("优惠券发放成功");
    }

    /**
     * 更新用户优惠券状态
     */
    @Override
    public Result<String> updateUserCouponStatus(@Valid @RequestBody CouponUpdateStatusRequest request) {
        userCouponService.updateUserCouponStatus(request);
        return Result.success("优惠券状态更新成功");
    }

    /**
     * 获取用户优惠券列表
     */
    @Override
    public Result<Page<UserCouponDTO>> getUserCoupons(
            @RequestBody UserCouponsRequest request
    ) {
        
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(request.getPageNum());
        pageRequest.setPageSize(request.getPageSize());
        
        Page<UserCouponDTO> page = userCouponService.getUserCoupons(pageRequest, request.getParams());
        return Result.success(page);
    }

    /**
     * 获取单个用户优惠券详情
     */
    @Override
    public Result<UserCouponDTO> getUserCoupon(@PathVariable Long couponId) {
        UserCouponDTO dto = userCouponService.getUserCoupon(couponId);
        return Result.success(dto);
    }

    /**
     * 统计用户优惠券
     */
    @Override
    public Result<Map<String, Object>> countUserCoupons(@PathVariable Long memberId) {
        Map<String, Object> result = userCouponService.countUserCoupons(memberId);
        return Result.success(result);
    }

    /**
     * 清理过期优惠券
     */
    @Override
    public Result<Integer> cleanExpiredCoupons() {
        int cleanedCount = userCouponService.cleanExpiredCoupons(LocalDateTime.now());
        return Result.success(cleanedCount, "清理过期优惠券 " + cleanedCount + " 张");
    }
}