package com.bbttcc.library.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "图书创建/更新请求")
public record BookRequest(

        @Schema(description = "ISBN", example = "9787111213826")
        @NotBlank(message = "ISBN 不能为空")
        @Size(max = 20, message = "ISBN 长度不能超过 20")
        String isbn,

        @Schema(description = "书名", example = "深入理解计算机系统")
        @NotBlank(message = "书名不能为空")
        @Size(max = 200, message = "书名长度不能超过 200")
        String title,

        @Schema(description = "作者", example = "Randal E. Bryant")
        @NotBlank(message = "作者不能为空")
        @Size(max = 120, message = "作者长度不能超过 120")
        String author,

        @Schema(description = "出版社", example = "机械工业出版社")
        @Size(max = 120, message = "出版社长度不能超过 120")
        String publisher,

        @Schema(description = "出版年份", example = "2016")
        @Min(value = 1000, message = "出版年份不合法")
        @Max(value = 2100, message = "出版年份不合法")
        Integer publishYear,

        @Schema(description = "分类 ID", example = "1")
        Long categoryId,

        @Schema(description = "馆藏总量", example = "5")
        @NotNull(message = "馆藏总量不能为空")
        @Min(value = 1, message = "馆藏总量至少为 1")
        Integer totalCopies,

        @Schema(description = "简介")
        @Size(max = 1000, message = "简介长度不能超过 1000")
        String description
) {
}
