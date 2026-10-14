package com.bbttcc.library.mapper;

import com.bbttcc.library.dto.borrow.BorrowResponse;
import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.BorrowRecord;
import com.bbttcc.library.entity.BorrowStatus;
import com.bbttcc.library.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BorrowMapper} 的纯单元测试（不依赖 Spring 容器）。
 */
class BorrowMapperTest {

    private static final double FINE_PER_DAY = 0.5;

    private BorrowRecord record(LocalDate borrowDate, LocalDate dueDate, BorrowStatus status) {
        User user = User.builder().id(1L).username("alice").build();
        Book book = Book.builder().id(10L).title("深入理解计算机系统").isbn("9787111544937").build();
        return BorrowRecord.builder()
                .id(100L).user(user).book(book)
                .borrowDate(borrowDate).dueDate(dueDate)
                .status(status).renewCount(0).fineAmount(0.0)
                .build();
    }

    @Test
    @DisplayName("未逾期时应归还罚金为 0")
    void fineIsZeroWhenNotOverdue() {
        LocalDate due = LocalDate.of(2026, 1, 31);
        BorrowRecord record = record(due.minusDays(30), due, BorrowStatus.BORROWED);

        double fine = BorrowMapper.calculateFine(record, LocalDate.of(2026, 1, 31), FINE_PER_DAY);

        assertThat(fine).isZero();
    }

    @Test
    @DisplayName("逾期 3 天且每天 0.5 元时罚金应为 1.5 元")
    void fineIsCalculatedForOverdueDays() {
        LocalDate due = LocalDate.of(2026, 1, 31);
        BorrowRecord record = record(due.minusDays(30), due, BorrowStatus.BORROWED);

        double fine = BorrowMapper.calculateFine(record, LocalDate.of(2026, 2, 3), FINE_PER_DAY);

        assertThat(fine).isEqualTo(1.5);
    }

    @Test
    @DisplayName("逾期天数应正确反映在响应中")
    void overdueDaysAppearInResponse() {
        LocalDate due = LocalDate.of(2026, 1, 31);
        BorrowRecord record = record(due.minusDays(30), due, BorrowStatus.BORROWED);

        BorrowResponse response = BorrowMapper.toResponse(record, LocalDate.of(2026, 2, 5));

        assertThat(response.overdueDays()).isEqualTo(5);
    }

    @Test
    @DisplayName("已归还的记录即使超过应还日期也不算逾期")
    void returnedRecordIsNotOverdue() {
        LocalDate due = LocalDate.of(2026, 1, 31);
        BorrowRecord record = record(due.minusDays(30), due, BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.of(2026, 2, 5));

        assertThat(BorrowMapper.isOverdue(record, LocalDate.of(2026, 2, 10))).isFalse();
        assertThat(BorrowMapper.toResponse(record, LocalDate.of(2026, 2, 10)).overdueDays()).isZero();
    }
}
