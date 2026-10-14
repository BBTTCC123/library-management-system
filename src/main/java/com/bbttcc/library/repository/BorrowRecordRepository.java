package com.bbttcc.library.repository;

import com.bbttcc.library.entity.BorrowRecord;
import com.bbttcc.library.entity.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>,
        JpaSpecificationExecutor<BorrowRecord> {

    /** 统计某用户当前未归还的记录数（借阅中 + 逾期）。 */
    long countByUserIdAndStatusIn(Long userId, Collection<BorrowStatus> statuses);

    /** 判断某用户是否已经借了某本书且尚未归还。 */
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<BorrowStatus> statuses);

    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);

    Page<BorrowRecord> findByStatus(BorrowStatus status, Pageable pageable);

    /** 查询所有已到期且尚未归还的记录（用于标记逾期）。 */
    @Query("select r from BorrowRecord r where r.status in :statuses and r.dueDate < :today")
    List<BorrowRecord> findOverdue(@Param("statuses") Collection<BorrowStatus> statuses,
                                   @Param("today") LocalDate today);

    long countByStatus(BorrowStatus status);

    long countByStatusIn(Collection<BorrowStatus> statuses);

    long countByBookIdAndStatusIn(Long bookId, Collection<BorrowStatus> statuses);
}
