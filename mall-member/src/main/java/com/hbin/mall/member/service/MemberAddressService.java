package com.hbin.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hbin.mall.api.order.dto.AddressDTO;
import com.hbin.mall.member.domain.MemberAddress;
import com.hbin.mall.member.dto.AddressCreateDTO;
import com.hbin.mall.member.dto.AddressUpdateDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MemberAddressService extends IService<MemberAddress> {

    List<AddressDTO> getAddressList(Long memberId);

    @Transactional
    void createAddress(Long memberId, AddressCreateDTO dto);

    @Transactional
    void updateAddress(Long memberId, AddressUpdateDTO dto);

    @Transactional
    void deleteAddress(Long memberId, Long addressId);

    @Transactional
    void setDefaultAddress(Long memberId, Long addressId);

    AddressDTO getDefaultAddress(Long memberId);
}
