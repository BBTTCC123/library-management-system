package com.bbttcc.library.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "分类创建/更新请求")
public record CategoryRequest(

        @Schema(description = "分类名称", example = "计算机")
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 80, message = "分类名称长度不能超过 80")
        String name,

        @Schema(description = "分类描述", example = "计算机科学与技术相关书籍")
        @Size(max = 255, message = "描述长度不能超过 255")
        String description
) {
}
