package com.bbttcc.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 借阅记录。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrow_records", indexes = {
        @Index(name = "idx_borrow_user", columnList = "user_id"),
        @Index(name = "idx_borrow_book", columnList = "book_id"),
        @Index(name = "idx_borrow_status", columnList = "status")
})
public class BorrowRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BorrowStatus status = BorrowStatus.BORROWED;

    @Column(name = "renew_count", nullable = false)
    @Builder.Default
    private int renewCount = 0;

    /** 逾期罚金（元），未逾期为 0。 */
    @Column(name = "fine_amount", nullable = false)
    @Builder.Default
    private double fineAmount = 0.0;

    /** 该记录是否仍未归还。 */
    public boolean isActive() {
        return status == BorrowStatus.BORROWED || status == BorrowStatus.OVERDUE;
    }
}
