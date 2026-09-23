package com.tokenmall.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tokenmall.seckill.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    @Update("""
            UPDATE seckill_activity
            SET sold_count = sold_count + #{quantity}
            WHERE id = #{activityId}
              AND deleted = 0
              AND sold_count + #{quantity} <= seckill_stock
            """)
    int deductStock(@Param("activityId") Long activityId, @Param("quantity") int quantity);
}
