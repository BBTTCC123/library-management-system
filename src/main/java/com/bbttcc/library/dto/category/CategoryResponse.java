package com.bbttcc.library.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分类信息")
public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
