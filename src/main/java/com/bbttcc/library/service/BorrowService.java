package com.bbttcc.library.service;

import com.bbttcc.library.config.BorrowProperties;
import com.bbttcc.library.dto.borrow.BorrowResponse;
import com.bbttcc.library.dto.borrow.LibraryStatsResponse;
import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.BorrowRecord;
import com.bbttcc.library.entity.BorrowStatus;
import com.bbttcc.library.entity.Role;
import com.bbttcc.library.entity.User;
import com.bbttcc.library.exception.BusinessException;
import com.bbttcc.library.exception.ResourceNotFoundException;
import com.bbttcc.library.mapper.BorrowMapper;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.BorrowRecordRepository;
import com.bbttcc.library.repository.CategoryRepository;
import com.bbttcc.library.repository.UserRepository;
import com.bbttcc.library.security.SecurityUser;
import com.bbttcc.library.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 借阅服务：借书、还书、续借、逾期计算与统计。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private static final EnumSet<BorrowStatus> ACTIVE_STATUSES =
            EnumSet.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE);

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowProperties properties;

    /**
     * 借书。
     *
     * <p>校验顺序：图书存在 -> 未重复借阅 -> 未超过借阅上限 -> 原子扣减库存。</p>
     */
    @Transactional
    public BorrowResponse borrow(Long bookId) {
        SecurityUser current = SecurityUtils.currentUser();
        User user = userRepository.findById(current.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("用户", current.getId()));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ResourceNotFoundException.of("图书", bookId));

        long activeCount = borrowRecordRepository.countByUserIdAndStatusIn(user.getId(), ACTIVE_STATUSES);
        if (activeCount >= properties.maxBooksPerUser()) {
            throw new BusinessException("已达到最大借阅数量(" + properties.maxBooksPerUser() + ")，请先归还部分图书");
        }

        if (borrowRecordRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), bookId, ACTIVE_STATUSES)) {
            throw new BusinessException("您已借阅该图书且尚未归还");
        }

        // 原子扣减库存，避免并发超借
        int updated = bookRepository.borrowOne(bookId);
        if (updated == 0) {
            throw new BusinessException("该图书暂无可借库存");
        }

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(today)
                .dueDate(today.plusDays(properties.loanPeriodDays()))
                .status(BorrowStatus.BORROWED)
                .renewCount(0)
                .fineAmount(0.0)
                .build();
        borrowRecordRepository.save(record);
        log.info("用户[{}]借阅图书[{}]", user.getUsername(), book.getTitle());
        return BorrowMapper.toResponse(record);
    }

    /**
     * 还书。普通读者只能归还自己的借阅记录，管理员/图书管理员可代还。
     */
    @Transactional
    public BorrowResponse returnBook(Long recordId) {
        BorrowRecord record = getEntity(recordId);
        checkOwnership(record);

        if (!record.isActive()) {
            throw new BusinessException("该借阅记录已归还，无需重复操作");
        }

        LocalDate today = LocalDate.now();
        double fine = BorrowMapper.calculateFine(record, today, properties.finePerDay());

        record.setReturnDate(today);
        record.setStatus(BorrowStatus.RETURNED);
        record.setFineAmount(fine);
        borrowRecordRepository.saveAndFlush(record);

        Long bookId = record.getBook().getId();
        // 原子更新会清空持久化上下文，需重新查询以拿到最新状态并安全地读取关联对象
        bookRepository.returnOne(bookId);

        BorrowRecord refreshed = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> ResourceNotFoundException.of("借阅记录", recordId));
        log.info("借阅记录[{}]已归还，罚金={}", recordId, fine);
        return BorrowMapper.toResponse(refreshed);
    }

    /**
     * 续借。仅未逾期且未超过续借次数的记录可续借。
     */
    @Transactional
    public BorrowResponse renew(Long recordId) {
        BorrowRecord record = getEntity(recordId);
        checkOwnership(record);

        if (!record.isActive()) {
            throw new BusinessException("该借阅记录已归还，无法续借");
        }
        LocalDate today = LocalDate.now();
        if (BorrowMapper.isOverdue(record, today)) {
            throw new BusinessException("图书已逾期，请先归还");
        }
        if (record.getRenewCount() >= properties.maxRenewCount()) {
            throw new BusinessException("已达到最大续借次数(" + properties.maxRenewCount() + ")");
        }

        record.setDueDate(record.getDueDate().plusDays(properties.renewPeriodDays()));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordRepository.save(record);
        return BorrowMapper.toResponse(record);
    }

    /**
     * 当前用户的借阅记录。
     */
    @Transactional(readOnly = true)
    public Page<BorrowResponse> myBorrows(BorrowStatus status, Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        return search(userId, null, status, pageable);
    }

    /**
     * 全部借阅记录（管理员/图书管理员）。
     */
    @Transactional(readOnly = true)
    public Page<BorrowResponse> search(Long userId, Long bookId, BorrowStatus status, Pageable pageable) {
        Specification<BorrowRecord> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (bookId != null) {
                predicates.add(cb.equal(root.get("book").get("id"), bookId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return borrowRecordRepository.findAll(spec, pageable).map(BorrowMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BorrowResponse findById(Long recordId) {
        BorrowRecord record = getEntity(recordId);
        checkOwnership(record);
        return BorrowMapper.toResponse(record);
    }

    @Transactional(readOnly = true)
    public LibraryStatsResponse stats() {
        return new LibraryStatsResponse(
                bookRepository.count(),
                categoryRepository.count(),
                userRepository.count(),
                borrowRecordRepository.countByStatusIn(ACTIVE_STATUSES),
                borrowRecordRepository.countByStatus(BorrowStatus.OVERDUE),
                borrowRecordRepository.countByStatus(BorrowStatus.RETURNED)
        );
    }

    /**
     * 每天凌晨刷新逾期状态与罚金（幂等）。
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void refreshOverdueStatus() {
        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdue = borrowRecordRepository.findOverdue(ACTIVE_STATUSES, today);
        for (BorrowRecord record : overdue) {
            record.setStatus(BorrowStatus.OVERDUE);
            record.setFineAmount(BorrowMapper.calculateFine(record, today, properties.finePerDay()));
        }
        if (!overdue.isEmpty()) {
            borrowRecordRepository.saveAll(overdue);
            log.info("刷新逾期借阅记录 {} 条", overdue.size());
        }
    }

    private BorrowRecord getEntity(Long id) {
        return borrowRecordRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("借阅记录", id));
    }

    private void checkOwnership(BorrowRecord record) {
        SecurityUser current = SecurityUtils.currentUser();
        boolean isOwner = record.getUser().getId().equals(current.getId());
        boolean isStaff = current.getRole() == Role.ADMIN || current.getRole() == Role.LIBRARIAN;
        if (!isOwner && !isStaff) {
            throw new org.springframework.security.access.AccessDeniedException("只能操作自己的借阅记录");
        }
    }
}
