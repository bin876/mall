package com.hbin.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.product.dto.AttrCreateDTO;
import com.hbin.mall.api.product.dto.AttrDto;
import com.hbin.mall.api.product.dto.AttrQueryDTO;
import com.hbin.mall.api.product.dto.AttrUpdateDTO;
import com.hbin.mall.product.domain.Attr;
import com.hbin.mall.product.mapper.AttrMapper;
import com.hbin.mall.product.service.AttrService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttrServiceImpl extends ServiceImpl<AttrMapper, Attr> implements AttrService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public Page<AttrDto> getAttrList(AttrQueryDTO query) {
        Page<Attr> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Attr> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getAttrName())) {
            wrapper.like(Attr::getAttrName, query.getAttrName());
        }
        if (query.getAttrType() != null) {
            wrapper.eq(Attr::getAttrType, query.getAttrType());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Attr::getCategoryId, query.getCategoryId());
        }

        wrapper.orderByDesc(Attr::getAttrId);
        Page<Attr> attrPage = attrMapper.selectPage(page, wrapper);

        return (Page<AttrDto>) attrPage.convert(attr -> {
            AttrDto dto = new AttrDto();
            BeanUtils.copyProperties(attr, dto);
            return dto;
        });
    }

    @Override
    public AttrDto getAttrById(Long attrId) {
        Attr attr = attrMapper.selectById(attrId);
        AttrDto attrDto = new AttrDto();
        BeanUtils.copyProperties(attr, attrDto);
        return attrDto;
    }

    @Override
    @Transactional
    public void createAttr(AttrCreateDTO dto) {
        if (attrMapper.exists(
                new LambdaQueryWrapper<Attr>()
                        .eq(Attr::getAttrName, dto.getAttrName())
                        .eq(Attr::getCategoryId, dto.getCategoryId())
                        .eq(Attr::getAttrType, dto.getAttrType())
        )) {
            throw new IllegalArgumentException("同分类下属性名称已存在");
        }

        Attr attr = new Attr();
        attr.setAttrName(dto.getAttrName());
        attr.setAttrType(dto.getAttrType());
        attr.setValueType(dto.getValueType());
        attr.setValueList(dto.getValueList());
        attr.setCategoryId(dto.getCategoryId());
        attrMapper.insert(attr);
    }

    @Override
    @Transactional
    public void updateAttr(AttrUpdateDTO dto) {
        Attr existing = attrMapper.selectById(dto.getAttrId());
        if (existing == null) {
            throw new IllegalArgumentException("属性不存在");
        }

        if (attrMapper.exists(
                new LambdaQueryWrapper<Attr>()
                        .eq(Attr::getAttrName, dto.getAttrName())
                        .eq(Attr::getCategoryId, dto.getCategoryId())
                        .eq(Attr::getAttrType, dto.getAttrType())
                        .ne(Attr::getAttrId, dto.getAttrId())
        )) {
            throw new IllegalArgumentException("同分类下属性名称已存在");
        }

        existing.setAttrName(dto.getAttrName());
        existing.setAttrType(dto.getAttrType());
        existing.setValueType(dto.getValueType());
        existing.setValueList(dto.getValueList());
        existing.setCategoryId(dto.getCategoryId());
        attrMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteAttrs(List<Long> attrIds) {
        attrMapper.deleteBatchIds(attrIds);
    }
}
