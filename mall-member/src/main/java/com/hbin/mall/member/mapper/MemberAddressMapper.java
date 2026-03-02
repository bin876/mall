package com.hbin.mall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbin.mall.member.domain.MemberAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MemberAddressMapper extends BaseMapper<MemberAddress> {

    @Update("UPDATE member_address SET is_default = 0 WHERE member_id = #{memberId} AND is_default = 1")
    void clearDefaultAddress(@Param("memberId") Long memberId);
}
