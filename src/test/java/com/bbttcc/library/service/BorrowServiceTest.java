package com.bbttcc.library.service;

import com.bbttcc.library.config.BorrowProperties;
import com.bbttcc.library.dto.borrow.BorrowResponse;
import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.BorrowRecord;
import com.bbttcc.library.entity.BorrowStatus;
import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.exception.BusinessException;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.BorrowRecordRepository;
import com.bbttcc.library.repository.CategoryRepository;
import com.bbttcc.library.repository.UserRepository;
import com.bbttcc.library.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BorrowService} 业务规则单元测试（Mockito 隔离依赖）。
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    private static final BorrowProperties PROPS = new BorrowProperties(3, 30, 15, 1, 0.5);

    @Mock
    private BorrowRecordRepository borrowRecordRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private BorrowService borrowService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        borrowService = new BorrowService(borrowRecordRepository, bookRepository, userRepository, categoryRepository, PROPS);
        user = User.builder().id(1L).username("alice").fullName("Alice").role(Role.MEMBER).enabled(true).build();
        book = Book.builder().id(10L).isbn("9787111544937").title("深入理解计算机系统")
                .totalCopies(3).availableCopies(3).build();
        authenticate(user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User u) {
        SecurityUser principal = SecurityUser.from(u);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    @DisplayName("正常借书：应创建 30 天期限的记录并扣减库存")
    void borrowSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserIdAndStatusIn(eq(1L), any())).thenReturn(0L);
        when(borrowRecordRepository.existsByUserIdAndBookIdAndStatusIn(eq(1L), eq(10L), any())).thenReturn(false);
        when(bookRepository.borrowOne(10L)).thenReturn(1);

        BorrowResponse response = borrowService.borrow(10L);

        assertThat(response.status()).isEqualTo(BorrowStatus.BORROWED);
        assertThat(response.borrowDate()).isEqualTo(LocalDate.now());
        assertThat(response.dueDate()).isEqualTo(LocalDate.now().plusDays(30));
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("超过最大借阅数量时应拒绝")
    void borrowRejectedWhenLimitReached() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserIdAndStatusIn(eq(1L), any())).thenReturn(3L);

        assertThatThrownBy(() -> borrowService.borrow(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最大借阅数量");

        verify(bookRepository, never()).borrowOne(anyLong());
    }

    @Test
    @DisplayName("重复借阅同一本书时应拒绝")
    void borrowRejectedWhenDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserIdAndStatusIn(eq(1L), any())).thenReturn(0L);
        when(borrowRecordRepository.existsByUserIdAndBookIdAndStatusIn(eq(1L), eq(10L), any())).thenReturn(true);

        assertThatThrownBy(() -> borrowService.borrow(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已借阅");
    }

    @Test
    @DisplayName("库存不足（原子更新返回 0）时应拒绝")
    void borrowRejectedWhenNoStock() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRecordRepository.countByUserIdAndStatusIn(eq(1L), any())).thenReturn(0L);
        when(borrowRecordRepository.existsByUserIdAndBookIdAndStatusIn(eq(1L), eq(10L), any())).thenReturn(false);
        when(bookRepository.borrowOne(10L)).thenReturn(0);

        assertThatThrownBy(() -> borrowService.borrow(10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("库存");

        verify(borrowRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("逾期归还时应计算罚金并回补库存")
    void returnWithFine() {
        BorrowRecord record = BorrowRecord.builder()
                .id(100L).user(user).book(book)
                .borrowDate(LocalDate.now().minusDays(33))
                .dueDate(LocalDate.now().minusDays(3))
                .status(BorrowStatus.BORROWED).renewCount(0).fineAmount(0.0)
                .build();
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(record));
        when(bookRepository.returnOne(10L)).thenReturn(1);

        BorrowResponse response = borrowService.returnBook(100L);

        assertThat(response.status()).isEqualTo(BorrowStatus.RETURNED);
        assertThat(response.fineAmount()).isEqualTo(1.5);
        assertThat(response.returnDate()).isEqualTo(LocalDate.now());
        verify(bookRepository).returnOne(10L);
    }

    @Test
    @DisplayName("续借成功：应延长 15 天且续借次数 +1")
    void renewSuccess() {
        BorrowRecord record = BorrowRecord.builder()
                .id(100L).user(user).book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10))
                .status(BorrowStatus.BORROWED).renewCount(0).fineAmount(0.0)
                .build();
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(record));

        BorrowResponse response = borrowService.renew(100L);

        assertThat(response.renewCount()).isEqualTo(1);
        assertThat(response.dueDate()).isEqualTo(LocalDate.now().plusDays(25));
    }

    @Test
    @DisplayName("已逾期时不允许续借")
    void renewRejectedWhenOverdue() {
        BorrowRecord record = BorrowRecord.builder()
                .id(100L).user(user).book(book)
                .borrowDate(LocalDate.now().minusDays(40))
                .dueDate(LocalDate.now().minusDays(10))
                .status(BorrowStatus.BORROWED).renewCount(0).fineAmount(0.0)
                .build();
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> borrowService.renew(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("逾期");
    }

    @Test
    @DisplayName("达到最大续借次数后不允许再次续借")
    void renewRejectedWhenMaxCountReached() {
        BorrowRecord record = BorrowRecord.builder()
                .id(100L).user(user).book(book)
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(5))
                .status(BorrowStatus.BORROWED).renewCount(1).fineAmount(0.0)
                .build();
        when(borrowRecordRepository.findById(100L)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> borrowService.renew(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("最大续借次数");
    }
}
