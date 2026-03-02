package com.hbin.mall.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.order.domain.CouponTemplate;
import com.hbin.mall.order.domain.UserCoupon;
import com.hbin.mall.order.mapper.CouponTemplateMapper;
import com.hbin.mall.order.mapper.UserCouponMapper;
import com.hbin.mall.order.service.UserCouponService;
import com.hbin.mall.order.vo.CouponTemplateVO;
import com.hbin.mall.order.vo.UserCouponVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coupon")
public class UserCouponController {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    @PostMapping("/receive")
    public Result<Void> receiveCoupon(@RequestBody Map<String, Long> request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        Long templateId = request.get("templateId");
        userCouponService.receiveCoupon(memberId, templateId);
        return Result.success();
    }

    @GetMapping("/available")
    public Result<List<UserCouponVO>> getAvailableCoupons(@RequestParam BigDecimal amount) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        List<UserCoupon> coupons = userCouponMapper.selectAvailableCoupons(memberId, amount);

        List<UserCouponVO> voList = coupons.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @GetMapping("/my")
    public Result<List<UserCouponVO>> getMyCoupons(@RequestParam(required = false) Integer status) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();

        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getMemberId, memberId);

        if (status != null) {
            wrapper.eq(UserCoupon::getStatus, status);
        }

        wrapper.orderByDesc(UserCoupon::getReceiveTime);
        List<UserCoupon> coupons = userCouponMapper.selectList(wrapper);

        List<UserCouponVO> voList = coupons.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    private UserCouponVO convertToVO(UserCoupon coupon) {
        UserCouponVO vo = new UserCouponVO();
        vo.setCouponId(coupon.getCouponId());
        vo.setTemplateId(coupon.getTemplateId());
        vo.setCouponCode(coupon.getCouponCode());
        vo.setStatus(coupon.getStatus());
        vo.setReceiveTime(coupon.getReceiveTime());
        vo.setUseTime(coupon.getUseTime());
        vo.setOrderSn(coupon.getOrderSn());
        vo.setExpireTime(coupon.getExpireTime());

        CouponTemplate template = couponTemplateMapper.selectById(coupon.getTemplateId());
        if (template != null) {
            CouponTemplateVO templateVO = new CouponTemplateVO();
            templateVO.setTemplateId(template.getTemplateId());
            templateVO.setName(template.getName());
            templateVO.setDescription(template.getDescription());
            templateVO.setCouponType(template.getCouponType());
            templateVO.setDiscountAmount(template.getDiscountAmount());
            templateVO.setMinOrderAmount(template.getMinOrderAmount());
            vo.setTemplate(templateVO);
        }

        return vo;
    }
}
