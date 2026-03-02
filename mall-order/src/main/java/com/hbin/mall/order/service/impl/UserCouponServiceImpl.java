package com.hbin.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.member.dto.MemberDTO;
import com.hbin.mall.api.member.feign.MemberFeignClient;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.common.exception.BizException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.domain.CouponTemplate;
import com.hbin.mall.order.domain.UserCoupon;
import com.hbin.mall.order.mapper.CouponTemplateMapper;
import com.hbin.mall.order.mapper.UserCouponMapper;
import com.hbin.mall.order.service.UserCouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private MemberFeignClient memberFeignClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String COUPON_LOCK_PREFIX = "lock:coupon:";

    @Override
    @Transactional
    public void receiveCoupon(Long memberId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券模板不存在或已禁用");
        }

        LocalDateTime now = LocalDateTime.now();
        if (template.getStartTime() != null && now.isBefore(template.getStartTime())) {
            throw new IllegalArgumentException("优惠券未开始发放");
        }
        if (template.getEndTime() != null && now.isAfter(template.getEndTime())) {
            throw new IllegalArgumentException("优惠券已结束发放");
        }

        Integer userCount = userCouponMapper.countByMemberAndTemplate(memberId, templateId);
        if (userCount >= template.getPerUserLimit()) {
            throw new IllegalArgumentException("超过每人领取限制");
        }

        if (template.getTotalCount() > 0) {
            Long usedCount = userCouponMapper.selectCount(
                    new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getTemplateId, templateId)
            );
            if (usedCount >= template.getTotalCount()) {
                throw new IllegalArgumentException("优惠券已抢光");
            }
        }

        UserCoupon coupon = new UserCoupon();
        coupon.setTemplateId(templateId);
        coupon.setMemberId(memberId);
        coupon.setCouponCode(generateCouponCode());
        coupon.setStatus(0);
        coupon.setReceiveTime(now);

        if (template.getValidDays() != null) {
            coupon.setExpireTime(now.plusDays(template.getValidDays()));
        } else {
            coupon.setExpireTime(template.getEndTime() != null ? template.getEndTime() : now.plusYears(1));
        }

        userCouponMapper.insert(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueCouponsToUsers(CouponIssueRequest request) {
        CouponTemplate template = couponTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }

        if (template.getStatus() == 0) {
            throw new BizException("优惠券模板已禁用，不可发放");
        }

        if (LocalDateTime.now().isAfter(template.getEndTime())) {
            throw new BizException("优惠券模板已过期，不可发放");
        }

        List<Long> invalidMemberIds = new ArrayList<>();
        for (Long memberId : request.getMemberIds()) {
            Result<MemberDTO> memberResult = memberFeignClient.getMemberDetail(memberId);
            if (memberResult == null || memberResult.getData() == null) {
                invalidMemberIds.add(memberId);
            }
        }

        if (!invalidMemberIds.isEmpty()) {
            throw new BizException("存在无效的用户ID: " + invalidMemberIds);
        }

        String lockKey = COUPON_LOCK_PREFIX + request.getTemplateId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException("系统繁忙，请稍后再试");
        }

        try {
            if (template.getTotalCount() != -1) {
                int issuedCount = userCouponMapper.countByTemplateId(request.getTemplateId());
                int remaining = template.getTotalCount() - issuedCount;
                if (remaining <= 0) {
                    throw new BizException("优惠券已发完");
                }

                if (request.getMemberIds().size() > remaining) {
                    request.setMemberIds(request.getMemberIds().subList(0, remaining));
                }
            }

            List<UserCoupon> coupons = new ArrayList<>();
            for (Long memberId : request.getMemberIds()) {
                int receivedCount = userCouponMapper.countByMemberAndTemplate(memberId, request.getTemplateId());
                if (receivedCount < template.getPerUserLimit()) {
                    UserCoupon coupon = new UserCoupon();
                    coupon.setTemplateId(request.getTemplateId());
                    coupon.setMemberId(memberId);
                    coupon.setCouponCode(generateCouponCode());
                    coupon.setStatus(0);
                    coupon.setReceiveTime(LocalDateTime.now());

                    LocalDateTime expireTime;
                    if (template.getValidDays() != null && template.getValidDays() > 0) {
                        expireTime = coupon.getReceiveTime().plusDays(template.getValidDays());
                    } else {
                        expireTime = template.getEndTime();
                    }
                    coupon.setExpireTime(expireTime);

                    coupons.add(coupon);
                }
            }

            if (!coupons.isEmpty()) {
                userCouponMapper.batchInsert(coupons);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private String generateCouponCode() {
        return "CP" + System.currentTimeMillis() + new Random().nextInt(1000000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserCouponStatus(CouponUpdateStatusRequest request) {
        UserCoupon coupon = userCouponMapper.selectById(request.getCouponId());
        if (coupon == null) {
            throw new BizException("优惠券不存在");
        }

        if (coupon.getStatus().intValue() == request.getStatus().intValue()) {
            return;
        }

        if (request.getStatus() == 1) {
            if (coupon.getStatus() != 0) {
                throw new BizException("只有未使用的优惠券才能标记为已使用");
            }

            if (request.getOrderSn() == null || request.getOrderSn().isEmpty()) {
                throw new BizException("使用订单号不能为空");
            }

            coupon.setUseTime(LocalDateTime.now());
            coupon.setOrderSn(request.getOrderSn());
        } else if (request.getStatus() == 2) {
            coupon.setUseTime(null);
            coupon.setOrderSn(null);
        }

        coupon.setStatus(request.getStatus());
        userCouponMapper.updateById(coupon);
    }

    @Override
    public Page<UserCouponDTO> getUserCoupons(PageRequest pageRequest, UserCouponQueryParams params) {
        Page<UserCoupon> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.getMemberId() != null) {
                wrapper.eq(UserCoupon::getMemberId, params.getMemberId());
            }
            if (params.getStatus() != null) {
                wrapper.eq(UserCoupon::getStatus, params.getStatus());
            }
            if (params.getStartTime() != null) {
                LocalDateTime startTime = params.getStartTime();
                wrapper.ge(UserCoupon::getReceiveTime, startTime);
            }
            if (params.getEndTime() != null) {
                LocalDateTime endTime = params.getEndTime();
                wrapper.le(UserCoupon::getReceiveTime, endTime);
            }
        }

        IPage<UserCoupon> userCouponPage = userCouponMapper.selectPage(page, wrapper);

        List<UserCouponDTO> dtoList = userCouponPage.getRecords().stream()
                .map(this::convertToUserCouponDTO)
                .collect(Collectors.toList());

        Page<UserCouponDTO> resultPage = new Page<>();
        resultPage.setCurrent(userCouponPage.getCurrent());
        resultPage.setSize(userCouponPage.getSize());
        resultPage.setTotal(userCouponPage.getTotal());
        resultPage.setPages(userCouponPage.getPages());
        resultPage.setRecords(dtoList);

        return resultPage;
    }

    private UserCouponDTO convertToUserCouponDTO(UserCoupon userCoupon) {
        UserCouponDTO dto = new UserCouponDTO();
        dto.setCouponId(userCoupon.getCouponId());
        dto.setTemplateId(userCoupon.getTemplateId());
        dto.setMemberId(userCoupon.getMemberId());
        dto.setCouponCode(userCoupon.getCouponCode());
        dto.setStatus(userCoupon.getStatus());
        dto.setReceiveTime(userCoupon.getReceiveTime());
        dto.setUseTime(userCoupon.getUseTime());
        dto.setOrderSn(userCoupon.getOrderSn());
        dto.setExpireTime(userCoupon.getExpireTime());

        try {
            Result<MemberDTO> memberResult = memberFeignClient.getMemberDetail(userCoupon.getMemberId());
            if (memberResult != null && memberResult.getData() != null) {
                MemberDTO member = memberResult.getData();
                dto.setMemberName(member.getNickname() != null ? member.getNickname() : member.getUsername());
                dto.setMemberPhone(member.getPhone());
            }
        } catch (Exception e) {
        }

        try {
            CouponTemplate template = couponTemplateMapper.selectById(userCoupon.getTemplateId());
            if (template != null) {
                dto.setTemplateName(template.getName());
                dto.setCouponType(template.getCouponType());
                dto.setDiscountAmount(template.getDiscountAmount());
                dto.setMinOrderAmount(template.getMinOrderAmount());
            }
        } catch (Exception e) {
        }

        dto.setStatusName(getStatusName(dto.getStatus()));
        dto.setCouponTypeName(getCouponTypeName(dto.getCouponType()));

        return dto;
    }

    @Override
    public UserCouponDTO getUserCoupon(Long couponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(couponId);
        if (userCoupon == null) {
            return null;
        }

        UserCouponDTO dto = new UserCouponDTO();
        dto.setCouponId(userCoupon.getCouponId());
        dto.setTemplateId(userCoupon.getTemplateId());
        dto.setMemberId(userCoupon.getMemberId());
        dto.setCouponCode(userCoupon.getCouponCode());
        dto.setStatus(userCoupon.getStatus());
        dto.setReceiveTime(userCoupon.getReceiveTime());
        dto.setUseTime(userCoupon.getUseTime());
        dto.setOrderSn(userCoupon.getOrderSn());
        dto.setExpireTime(userCoupon.getExpireTime());

        CouponTemplate template = couponTemplateMapper.selectById(userCoupon.getTemplateId());
        if (template != null) {
            dto.setTemplateName(template.getName());
            dto.setCouponType(template.getCouponType());
            dto.setDiscountAmount(template.getDiscountAmount());
            dto.setMinOrderAmount(template.getMinOrderAmount());
        }

        // 获取用户信息
        Result<MemberDTO> memberResult = memberFeignClient.getMemberDetail(userCoupon.getMemberId());
        if (memberResult != null && memberResult.getData() != null) {
            MemberDTO member = memberResult.getData();
            dto.setMemberName(member.getNickname() != null ? member.getNickname() : member.getUsername());
            dto.setMemberPhone(member.getPhone());
        }

        dto.setStatusName(getStatusName(userCoupon.getStatus()));

        return dto;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "未使用";
            case 1 -> "已使用";
            case 2 -> "已过期";
            default -> "未知状态";
        };
    }

    private String getCouponTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "满减券";
            case 2 -> "折扣券";
            case 3 -> "无门槛券";
            default -> "未知类型";
        };
    }

    @Override
    public Map<String, Object> countUserCoupons(Long memberId) {
        Map<String, Object> result = new HashMap<>();

        int totalCount = userCouponMapper.countByMemberId(memberId);
        result.put("totalCount", totalCount);

        int availableCount = userCouponMapper.countAvailableByMemberId(memberId, LocalDateTime.now());
        result.put("availableCount", availableCount);

        int usedCount = userCouponMapper.countUsedByMemberId(memberId);
        result.put("usedCount", usedCount);

        List<Map<String, Object>> typeStats = userCouponMapper.countByMemberAndType(memberId);
        Map<Integer, Integer> typeCountMap = typeStats.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("couponType")).intValue(),
                        map -> ((Number) map.get("count")).intValue()
                ));

        result.put("typeStats", typeCountMap);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredCoupons(LocalDateTime currentTime) {
        return userCouponMapper.updateExpiredCoupons(currentTime);
    }
}
