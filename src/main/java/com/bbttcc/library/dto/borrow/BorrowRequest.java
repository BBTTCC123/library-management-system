package com.bbttcc.library.dto.borrow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "借书请求")
public record BorrowRequest(

        @Schema(description = "图书 ID", example = "1")
        @NotNull(message = "图书 ID 不能为空")
        Long bookId
) {
}
