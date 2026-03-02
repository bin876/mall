package com.hbin.mall.member.controller;

import com.hbin.mall.api.order.dto.AddressDTO;
import com.hbin.mall.common.result.Result;
import com.hbin.mall.common.satoken.StpMemberUtil;
import com.hbin.mall.member.domain.MemberAddress;
import com.hbin.mall.member.dto.AddressCreateDTO;
import com.hbin.mall.member.dto.AddressUpdateDTO;
import com.hbin.mall.member.service.MemberAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/address")
@RequiredArgsConstructor
public class AddressController {

    @Autowired
    private MemberAddressService addressService;

    /**
     * 获取地址列表
     */
    @GetMapping("/list")
    public Result<List<AddressDTO>> getAddressList() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        List<AddressDTO> addresses = addressService.getAddressList(memberId);
        return Result.success(addresses);
    }

    /**
     * 创建地址
     */
    @PostMapping
    public Result<String> createAddress(@Valid @RequestBody AddressCreateDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        addressService.createAddress(memberId, dto);
        return Result.success("地址创建成功");
    }

    /**
     * 更新地址
     */
    @PutMapping
    public Result<String> updateAddress(@Valid @RequestBody AddressUpdateDTO dto) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        addressService.updateAddress(memberId, dto);
        return Result.success("地址更新成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{addressId}")
    public Result<String> deleteAddress(@PathVariable Long addressId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        addressService.deleteAddress(memberId, addressId);
        return Result.success("地址删除成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{addressId}/default")
    public Result<String> setDefaultAddress(@PathVariable Long addressId) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        addressService.setDefaultAddress(memberId, addressId);
        return Result.success("默认地址设置成功");
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result<AddressDTO> getDefaultAddress() {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        AddressDTO address = addressService.getDefaultAddress(memberId);
        return Result.success(address);
    }
}