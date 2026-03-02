package com.hbin.mall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbin.mall.member.domain.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT COUNT(*) > 0 FROM notification WHERE member_id = #{memberId} AND related_id = #{relatedId} AND type = #{type} AND category = #{category}")
    boolean existsByUniqueKey(@Param("memberId") Long memberId,
                              @Param("relatedId") Long relatedId,
                              @Param("type") Integer type,
                              @Param("category") String category);
}
