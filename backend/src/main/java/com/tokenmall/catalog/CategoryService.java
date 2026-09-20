package com.tokenmall.catalog;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.catalog.dto.CategoryRequest;
import com.tokenmall.catalog.entity.ProductCategory;
import com.tokenmall.catalog.mapper.ProductCategoryMapper;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProductCategoryMapper categoryMapper;

    public List<ProductCategory> listEnabled() {
        return categoryMapper.selectList(
                Wrappers.<ProductCategory>lambdaQuery()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder)
        );
    }

    public List<ProductCategory> listAll() {
        return categoryMapper.selectList(
                Wrappers.<ProductCategory>lambdaQuery().orderByAsc(ProductCategory::getSortOrder)
        );
    }

    public ProductCategory create(CategoryRequest request) {
        ProductCategory category = new ProductCategory();
        category.setName(request.name());
        category.setCode(request.code());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status() == null ? 1 : request.status());
        category.setDeleted(0);
        categoryMapper.insert(category);
        return category;
    }

    public ProductCategory update(Long id, CategoryRequest request) {
        ProductCategory category = get(id);
        category.setName(request.name());
        category.setCode(request.code());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status() == null ? 1 : request.status());
        categoryMapper.updateById(category);
        return category;
    }

    public void delete(Long id) {
        get(id);
        categoryMapper.deleteById(id);
    }

    private ProductCategory get(Long id) {
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }
}
