package com.bbttcc.library.mapper;

import com.bbttcc.library.dto.category.CategoryResponse;
import com.bbttcc.library.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
