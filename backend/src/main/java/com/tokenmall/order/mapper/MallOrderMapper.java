package com.tokenmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tokenmall.order.entity.MallOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallOrderMapper extends BaseMapper<MallOrder> {
}
