package com.bbttcc.library.repository;

import com.bbttcc.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    long countByCategoryId(Long categoryId);

    /**
     * 原子性地借出一本：仅当 {@code availableCopies > 0} 时才会成功自减。
     *
     * @return 受影响行数，1 表示借出成功，0 表示库存不足（并发安全）
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.availableCopies = b.availableCopies - 1 "
            + "where b.id = :id and b.availableCopies > 0")
    int borrowOne(@Param("id") Long id);

    /**
     * 原子性地归还一本：可借数量不会超过馆藏总量。
     *
     * @return 受影响行数
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Book b set b.availableCopies = b.availableCopies + 1 "
            + "where b.id = :id and b.availableCopies < b.totalCopies")
    int returnOne(@Param("id") Long id);
}
