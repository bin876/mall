package com.hbin.mall.common.constant;

// Redis键前缀
public class RedisKey {
    // 商品库存 key: seckill:stock:{seckillSkuId}
    public static final String SECKILL_STOCK = "seckill:stock:";
    
    // 活动预热状态 key: seckill:preheat:{activityId}
    public static final String SECKILL_PREHEAT = "seckill:preheat:";
    
    // 用户已购买记录 key: seckill:bought:{memberId}:{seckillSkuId}
    public static final String SECKILL_BOUGHT = "seckill:bought:";
}