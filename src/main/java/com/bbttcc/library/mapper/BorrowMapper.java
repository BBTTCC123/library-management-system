package com.bbttcc.library.mapper;

import com.bbttcc.library.dto.borrow.BorrowResponse;
import com.bbttcc.library.entity.BorrowRecord;
import com.bbttcc.library.entity.BorrowStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class BorrowMapper {

    private BorrowMapper() {
    }

    public static BorrowResponse toResponse(BorrowRecord record) {
        return toResponse(record, LocalDate.now());
    }

    /**
     * @param today 用于计算逾期天数的“今天”，便于测试注入固定日期
     */
    public static BorrowResponse toResponse(BorrowRecord record, LocalDate today) {
        long overdueDays = 0;
        if (record.isActive() && today.isAfter(record.getDueDate())) {
            overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
        }
        return new BorrowResponse(
                record.getId(),
                record.getBook().getId(),
                record.getBook().getTitle(),
                record.getBook().getIsbn(),
                record.getUser().getId(),
                record.getUser().getUsername(),
                record.getBorrowDate(),
                record.getDueDate(),
                record.getReturnDate(),
                record.getStatus(),
                record.getRenewCount(),
                record.getFineAmount(),
                overdueDays
        );
    }

    /** 计算逾期罚金。 */
    public static double calculateFine(BorrowRecord record, LocalDate returnDate, double finePerDay) {
        if (!returnDate.isAfter(record.getDueDate())) {
            return 0.0;
        }
        long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), returnDate);
        return Math.round(overdueDays * finePerDay * 100.0) / 100.0;
    }

    public static boolean isOverdue(BorrowRecord record, LocalDate today) {
        return record.isActive() && today.isAfter(record.getDueDate());
    }

    public static BorrowStatus effectiveStatus(BorrowRecord record, LocalDate today) {
        if (record.isActive() && today.isAfter(record.getDueDate())) {
            return BorrowStatus.OVERDUE;
        }
        return record.getStatus();
    }
}
