package com.hbin.mall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hbin.mall.order.domain.OrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    int insertBatchSomeColumn(@Param("list") List<OrderItem> batchList);

    @Select({
            "<script>",
            "SELECT order_id, COUNT(DISTINCT sku_id) as count ",
            "FROM order_item ",
            "WHERE order_id IN ",
            "<foreach collection='orderIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            " GROUP BY order_id",
            "</script>"
    })
    List<Map<String, Object>> selectProductCountByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 批量查询订单首个商品图片
     */
    @Select({
            "<script>",
            "SELECT order_id, sku_pic ",
            "FROM (",
            "  SELECT order_id, sku_pic, ROW_NUMBER() OVER (PARTITION BY order_id ORDER BY item_id) as rn",
            "  FROM order_item",
            "  WHERE order_id IN ",
            "  <foreach collection='orderIds' item='id' open='(' separator=',' close=')'>",
            "    #{id}",
            "  </foreach>",
            ") t WHERE rn = 1",
            "</script>"
    })
    List<Map<String, Object>> selectFirstProductImgByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectListByOrderId(@Param("orderId") Long orderId);

    @Delete("DELETE FROM order_item WHERE order_id = #{orderId}")
    void deleteByOrderId(@Param("orderId") Long orderId);
}
