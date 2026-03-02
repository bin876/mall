package com.hbin.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hbin.mall.api.order.dto.AddressDTO;
import com.hbin.mall.common.exception.BusinessException;
import com.hbin.mall.member.domain.MemberAddress;
import com.hbin.mall.member.dto.AddressCreateDTO;
import com.hbin.mall.member.dto.AddressUpdateDTO;
import com.hbin.mall.member.mapper.MemberAddressMapper;
import com.hbin.mall.member.service.MemberAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberAddressServiceImpl extends ServiceImpl<MemberAddressMapper, MemberAddress> implements MemberAddressService {
    
    @Autowired
    private MemberAddressMapper addressMapper;

    @Override
    public List<AddressDTO> getAddressList(Long memberId) {
        LambdaQueryWrapper<MemberAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAddress::getMemberId, memberId)
                .orderByDesc(MemberAddress::getIsDefault)
                .orderByDesc(MemberAddress::getUpdateTime);

        List<MemberAddress> addresses = addressMapper.selectList(wrapper);
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createAddress(Long memberId, AddressCreateDTO dto) {
        validateAddress(dto);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultAddress(memberId);
        }

        MemberAddress address = new MemberAddress();
        address.setMemberId(memberId);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setReceiverProvince(dto.getReceiverProvince());
        address.setReceiverCity(dto.getReceiverCity());
        address.setReceiverRegion(dto.getReceiverRegion());
        address.setReceiverDetailAddress(dto.getReceiverDetailAddress());
        address.setReceiverPostCode(dto.getReceiverPostCode());
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void updateAddress(Long memberId, AddressUpdateDTO dto) {
        MemberAddress existing = addressMapper.selectById(dto.getAddressId());
        if (existing == null || !existing.getMemberId().equals(memberId)) {
            throw new BusinessException("地址不存在或无权修改");
        }

        validateAddressForUpdate(dto);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            clearDefaultAddress(memberId);
        }

        MemberAddress address = new MemberAddress();
        address.setAddressId(dto.getAddressId());
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setReceiverProvince(dto.getReceiverProvince());
        address.setReceiverCity(dto.getReceiverCity());
        address.setReceiverRegion(dto.getReceiverRegion());
        address.setReceiverDetailAddress(dto.getReceiverDetailAddress());
        address.setReceiverPostCode(dto.getReceiverPostCode());
        address.setIsDefault(Boolean.TRUE.equals(dto.getIsDefault()) ? 1 : 0);
        address.setUpdateTime(LocalDateTime.now());

        addressMapper.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {
        MemberAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new BusinessException("地址不存在或无权删除");
        }

        if (address.getIsDefault() == 1) {
            setNewDefaultAddress(memberId, addressId);
        }

        addressMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long memberId, Long addressId) {
        MemberAddress address = addressMapper.selectById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new BusinessException("地址不存在或无权设置");
        }

        clearDefaultAddress(memberId);

        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    public AddressDTO getDefaultAddress(Long memberId) {
        MemberAddress address = addressMapper.selectOne(
                new LambdaQueryWrapper<MemberAddress>()
                        .eq(MemberAddress::getMemberId, memberId)
                        .eq(MemberAddress::getIsDefault, 1)
        );

        return address != null ? convertToDTO(address) : null;
    }

    private void clearDefaultAddress(Long memberId) {
        addressMapper.clearDefaultAddress(memberId);
    }

    private void setNewDefaultAddress(Long memberId, Long excludeAddressId) {
        List<MemberAddress> otherAddresses = addressMapper.selectList(
                new LambdaQueryWrapper<MemberAddress>()
                        .eq(MemberAddress::getMemberId, memberId)
                        .ne(MemberAddress::getAddressId, excludeAddressId)
                        .orderByDesc(MemberAddress::getUpdateTime)
        );

        if (!otherAddresses.isEmpty()) {
            MemberAddress newDefault = otherAddresses.get(0);
            newDefault.setIsDefault(1);
            newDefault.setUpdateTime(LocalDateTime.now());
            addressMapper.updateById(newDefault);
        }
    }

    private void validateAddress(AddressCreateDTO dto) {
        if (dto.getReceiverName() == null || dto.getReceiverName().trim().isEmpty()) {
            throw new BusinessException("收货人姓名不能为空");
        }
        if (dto.getReceiverPhone() == null || !dto.getReceiverPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        if (dto.getReceiverProvince() == null || dto.getReceiverProvince().trim().isEmpty()) {
            throw new BusinessException("省份不能为空");
        }
        if (dto.getReceiverCity() == null || dto.getReceiverCity().trim().isEmpty()) {
            throw new BusinessException("城市不能为空");
        }
        if (dto.getReceiverRegion() == null || dto.getReceiverRegion().trim().isEmpty()) {
            throw new BusinessException("区/县不能为空");
        }
        if (dto.getReceiverDetailAddress() == null || dto.getReceiverDetailAddress().trim().isEmpty()) {
            throw new BusinessException("详细地址不能为空");
        }
    }

    private void validateAddressForUpdate(AddressUpdateDTO dto) {
        if (dto.getAddressId() == null) {
            throw new BusinessException("地址ID不能为空");
        }

        AddressCreateDTO createDto = new AddressCreateDTO();
        createDto.setReceiverName(dto.getReceiverName());
        createDto.setReceiverPhone(dto.getReceiverPhone());
        createDto.setReceiverProvince(dto.getReceiverProvince());
        createDto.setReceiverCity(dto.getReceiverCity());
        createDto.setReceiverRegion(dto.getReceiverRegion());
        createDto.setReceiverDetailAddress(dto.getReceiverDetailAddress());
        createDto.setReceiverPostCode(dto.getReceiverPostCode());
        validateAddress(createDto);
    }

    private AddressDTO convertToDTO(MemberAddress address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddressId(address.getAddressId());
        dto.setReceiverName(address.getReceiverName());
        dto.setReceiverPhone(address.getReceiverPhone());
        dto.setReceiverProvince(address.getReceiverProvince());
        dto.setReceiverCity(address.getReceiverCity());
        dto.setReceiverRegion(address.getReceiverRegion());
        dto.setReceiverDetailAddress(address.getReceiverDetailAddress());
        dto.setReceiverPostCode(address.getReceiverPostCode());
        dto.setIsDefault(address.getIsDefault() == 1);
        return dto;
    }
}
