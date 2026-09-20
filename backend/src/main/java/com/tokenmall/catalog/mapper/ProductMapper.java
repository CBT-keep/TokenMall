package com.tokenmall.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tokenmall.catalog.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
