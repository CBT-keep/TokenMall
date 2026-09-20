package com.tokenmall.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tokenmall.catalog.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}
