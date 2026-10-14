package com.bbttcc.library.dto.borrow;

import com.bbttcc.library.entity.BorrowStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "借阅记录信息")
public record BorrowResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String isbn,
        Long userId,
        String username,
        LocalDate borrowDate,
        LocalDate dueDate,
        LocalDate returnDate,
        BorrowStatus status,
        int renewCount,
        double fineAmount,
        @Schema(description = "已逾期天数，未逾期为 0") long overdueDays
) {
}
