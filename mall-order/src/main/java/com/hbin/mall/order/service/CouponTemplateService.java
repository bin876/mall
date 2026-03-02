package com.hbin.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.*;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.order.domain.CouponTemplate;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

public interface CouponTemplateService extends IService<CouponTemplate> {

    @Transactional(rollbackFor = Exception.class)
    void createCouponTemplate(CouponTemplateCreateRequest request);

    @Transactional(rollbackFor = Exception.class)
    void updateCouponTemplate(CouponTemplateUpdateRequest request);

    @Transactional(rollbackFor = Exception.class)
    void deleteCouponTemplate(Long templateId);

    Page<CouponTemplateDTO> getCouponTemplates(PageRequest pageRequest, CouponTemplateQueryParams params);

    CouponTemplateDTO getCouponTemplate(Long templateId);

    @Transactional(rollbackFor = Exception.class)
    void updateTemplateStatus(Long templateId, Integer status);
}
