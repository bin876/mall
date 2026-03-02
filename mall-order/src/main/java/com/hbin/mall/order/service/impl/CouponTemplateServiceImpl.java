package com.hbin.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.common.exception.BizException;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.domain.CouponTemplate;
import com.hbin.mall.order.mapper.CouponTemplateMapper;
import com.hbin.mall.order.mapper.UserCouponMapper;
import com.hbin.mall.order.service.CouponTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplate> implements CouponTemplateService {

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCouponTemplate(CouponTemplateCreateRequest request) {
        try {
            validateCouponTemplate(request);

            CouponTemplate template = new CouponTemplate();
            template.setName(request.getName());
            template.setDescription(request.getDescription());
            template.setCouponType(request.getCouponType());
            template.setDiscountAmount(new BigDecimal(request.getDiscountAmount().toString()));
            template.setMinOrderAmount(request.getMinOrderAmount() != null ?
                    new BigDecimal(request.getMinOrderAmount().toString()) : null);
            template.setTotalCount(request.getTotalCount() != null ? request.getTotalCount() : -1);
            template.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
            template.setValidDays(request.getValidDays());
            template.setStartTime(request.getStartTime());
            template.setEndTime(request.getEndTime());
            template.setStatus(request.getStatus() != null ? request.getStatus() : 1);
            template.setCreateTime(LocalDateTime.now());

            couponTemplateMapper.insert(template);
        } catch (Exception e) {
            log.error("创建优惠券模板失败", e);
            throw new BizException("创建优惠券模板失败");
        }
    }

    private void validateCouponTemplate(CouponTemplateCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BizException("优惠券名称不能为空");
        }

        if (request.getCouponType() == null || !List.of(1, 2, 3).contains(request.getCouponType())) {
            throw new BizException("优惠券类型不合法");
        }

        if (request.getDiscountAmount() == null || request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("优惠金额/折扣比例不能为空且必须大于0");
        }

        if (request.getCouponType() != 3) {
            if (request.getMinOrderAmount() == null || request.getMinOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("最低订单金额不能为空且必须大于0");
            }
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BizException("有效期不能为空");
        }

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BizException("开始时间不能晚于结束时间");
        }

        if (request.getPerUserLimit() == null || request.getPerUserLimit() <= 0) {
            throw new BizException("每人限领数量必须大于0");
        }

        if (request.getValidDays() != null && request.getValidDays() <= 0) {
            throw new BizException("有效天数必须大于0");
        }
    }

    private void validateCouponAmount(CouponTemplateCreateRequest request) {
        if (request.getCouponType() == 3) {
            // 无门槛券
            if (request.getDiscountAmount() == null || request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("优惠金额不能为空且必须大于0");
            }
            return;
        }

        // 满减券或折扣券
        if (request.getDiscountAmount() == null || request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("优惠金额/折扣比例不能为空且必须大于0");
        }

        if (request.getMinOrderAmount() == null || request.getMinOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("最低订单金额不能为空且必须大于0");
        }

        if (request.getCouponType() == 1) {
            // 满减券
            if (request.getDiscountAmount().compareTo(request.getMinOrderAmount()) >= 0) {
                throw new BizException("优惠金额不能大于或等于最低订单金额");
            }
            if (request.getDiscountAmount().compareTo(new BigDecimal("10000")) > 0) {
                throw new BizException("满减金额不能超过10000元");
            }
        } else if (request.getCouponType() == 2) {
            // 折扣券
            if (request.getDiscountAmount().compareTo(new BigDecimal("0.1")) < 0 ||
                    request.getDiscountAmount().compareTo(BigDecimal.ONE) >= 0) {
                throw new BizException("折扣比例必须在0.1-1之间");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCouponTemplate(CouponTemplateUpdateRequest request) {
        CouponTemplate existing = couponTemplateMapper.selectById(request.getTemplateId());
        if (existing == null) {
            throw new BizException("优惠券模板不存在");
        }

        int issuedCount = userCouponMapper.countByTemplateId(request.getTemplateId());
        if (issuedCount > 0) {
            if (!existing.getName().equals(request.getName()) ||
                    existing.getCouponType().intValue() != request.getCouponType().intValue() ||
                    !existing.getDiscountAmount().equals(request.getDiscountAmount()) ||
                    !existing.getMinOrderAmount().equals(request.getMinOrderAmount()) ||
                    (existing.getValidDays() != null && !existing.getValidDays().equals(request.getValidDays()))) {
                throw new BizException("已有用户领取该优惠券，部分字段不可修改");
            }
        }

        validateCouponTemplateForUpdate(request);

        CouponTemplate template = new CouponTemplate();
        template.setTemplateId(request.getTemplateId());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCouponType(request.getCouponType());
        template.setDiscountAmount(request.getDiscountAmount());
        template.setMinOrderAmount(request.getMinOrderAmount());
        template.setTotalCount(request.getTotalCount() != null ? request.getTotalCount() : -1);
        template.setPerUserLimit(request.getPerUserLimit());
        template.setValidDays(request.getValidDays());
        template.setStartTime(request.getStartTime());
        template.setEndTime(request.getEndTime());
        template.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());

        couponTemplateMapper.updateById(template);
    }

    private void validateCouponTemplateForUpdate(CouponTemplateUpdateRequest request) {
        if (request.getTemplateId() == null) {
            throw new BizException("模板ID不能为空");
        }

        CouponTemplateCreateRequest createRequest = new CouponTemplateCreateRequest();
        createRequest.setName(request.getName());
        createRequest.setDescription(request.getDescription());
        createRequest.setCouponType(request.getCouponType());
        createRequest.setDiscountAmount(request.getDiscountAmount());
        createRequest.setMinOrderAmount(request.getMinOrderAmount());
        createRequest.setTotalCount(request.getTotalCount());
        createRequest.setPerUserLimit(request.getPerUserLimit());
        createRequest.setValidDays(request.getValidDays());
        createRequest.setStartTime(request.getStartTime());
        createRequest.setEndTime(request.getEndTime());
        createRequest.setStatus(request.getStatus());

        validateCouponTemplate(createRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCouponTemplate(Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }

        int issuedCount = userCouponMapper.countByTemplateId(templateId);
        if (issuedCount > 0) {
            throw new BizException("已有用户领取该优惠券，不可删除");
        }

        couponTemplateMapper.deleteById(templateId);
    }

    @Override
    public Page<CouponTemplateDTO> getCouponTemplates(PageRequest pageRequest, CouponTemplateQueryParams params) {
        Page<CouponTemplate> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());

        LambdaQueryWrapper<CouponTemplate> wrapper = new LambdaQueryWrapper<>();

        if (params != null) {
            if (params.getName() != null && !params.getName().isEmpty()) {
                wrapper.like(CouponTemplate::getName, params.getName());
            }
            if (params.getCouponType() != null) {
                wrapper.eq(CouponTemplate::getCouponType, params.getCouponType());
            }
            if (params.getStatus() != null) {
                wrapper.eq(CouponTemplate::getStatus, params.getStatus());
            }
            if (params.getStartTime() != null) {
                try {
                    LocalDateTime startTime = params.getStartTime();
                    wrapper.ge(CouponTemplate::getCreateTime, startTime);
                } catch (Exception e) {
                }
            }
            if (params.getEndTime() != null) {
                LocalDateTime endTime = params.getEndTime();
                wrapper.le(CouponTemplate::getCreateTime, endTime);
            }
        }

        wrapper.orderByDesc(CouponTemplate::getCreateTime);

        IPage<CouponTemplate> templatePage = couponTemplateMapper.selectPage(page, wrapper);

        return (Page<CouponTemplateDTO>) templatePage.convert(this::convertToDTO);
    }
    private CouponTemplateDTO convertToDTO(CouponTemplate template) {
        CouponTemplateDTO dto = new CouponTemplateDTO();
        dto.setTemplateId(template.getTemplateId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setCouponType(template.getCouponType());
        dto.setCouponTypeName(getCouponTypeName(template.getCouponType()));
        dto.setDiscountAmount(template.getDiscountAmount());
        dto.setMinOrderAmount(template.getMinOrderAmount());
        dto.setTotalCount(template.getTotalCount());
        dto.setPerUserLimit(template.getPerUserLimit());
        dto.setValidDays(template.getValidDays());
        dto.setStartTime(template.getStartTime());
        dto.setEndTime(template.getEndTime());
        dto.setStatus(template.getStatus());
        dto.setStatusName(template.getStatus() == 1 ? "启用" : "禁用");
        dto.setCreateTime(template.getCreateTime());

        int issuedCount = couponTemplateMapper.countIssuedByTemplateId(template.getTemplateId());
        dto.setIssuedCount(issuedCount);

        return dto;
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
    public CouponTemplateDTO getCouponTemplate(Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            return null;
        }
        return convertToDTO(template);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplateStatus(Long templateId, Integer status) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }

        if (template.getStatus() == status) {
            return;
        }

        template.setStatus(status);
        couponTemplateMapper.updateById(template);
    }
}
