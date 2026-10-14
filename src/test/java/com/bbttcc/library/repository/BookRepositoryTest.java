package com.bbttcc.library.repository;

import com.bbttcc.library.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BookRepository} 库存原子更新测试。
 *
 * <p>JPA 审计由主类 {@code LibraryApplication} 上的 {@code @EnableJpaAuditing} 提供。</p>
 */
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    private Book book;

    @BeforeEach
    void setUp() {
        book = bookRepository.save(Book.builder()
                .isbn("9787111544937").title("深入理解计算机系统").author("Bryant")
                .totalCopies(2).availableCopies(2).build());
    }

    @Test
    @DisplayName("借出应使可借数量减 1")
    void borrowOneDecrements() {
        int updated = bookRepository.borrowOne(book.getId());

        assertThat(updated).isEqualTo(1);
        assertThat(reload().getAvailableCopies()).isEqualTo(1);
    }

    @Test
    @DisplayName("库存为 0 时借出应失败且不影响数据")
    void borrowOneFailsWhenOutOfStock() {
        bookRepository.borrowOne(book.getId());
        bookRepository.borrowOne(book.getId());

        int updated = bookRepository.borrowOne(book.getId());

        assertThat(updated).isZero();
        assertThat(reload().getAvailableCopies()).isZero();
    }

    @Test
    @DisplayName("归还不应使可借数量超过馆藏总量")
    void returnOneNeverExceedsTotal() {
        int updated = bookRepository.returnOne(book.getId());

        assertThat(updated).isZero();
        assertThat(reload().getAvailableCopies()).isEqualTo(2);
    }

    private Book reload() {
        return bookRepository.findById(book.getId()).orElseThrow();
    }
}
