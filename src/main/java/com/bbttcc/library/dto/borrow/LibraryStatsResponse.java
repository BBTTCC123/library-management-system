package com.bbttcc.library.dto.borrow;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "图书馆整体统计信息")
public record LibraryStatsResponse(
        long totalBooks,
        long totalCategories,
        long totalUsers,
        long activeBorrows,
        long overdueBorrows,
        long returnedBorrows
) {
}
