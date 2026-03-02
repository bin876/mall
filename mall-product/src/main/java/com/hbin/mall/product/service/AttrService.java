package com.hbin.mall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.product.dto.AttrCreateDTO;
import com.hbin.mall.api.product.dto.AttrDto;
import com.hbin.mall.api.product.dto.AttrQueryDTO;
import com.hbin.mall.api.product.dto.AttrUpdateDTO;
import com.hbin.mall.product.domain.Attr;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AttrService extends IService<Attr> {

    Page<AttrDto> getAttrList(AttrQueryDTO query);

    AttrDto getAttrById(Long attrId);

    @Transactional
    void createAttr(AttrCreateDTO dto);

    @Transactional
    void updateAttr(AttrUpdateDTO dto);

    @Transactional
    void deleteAttrs(List<Long> attrIds);
}
