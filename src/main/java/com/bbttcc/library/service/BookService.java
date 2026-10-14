package com.bbttcc.library.service;

import com.bbttcc.library.dto.book.BookRequest;
import com.bbttcc.library.dto.book.BookResponse;
import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.BorrowStatus;
import com.bbttcc.library.entity.Category;
import com.bbttcc.library.exception.BusinessException;
import com.bbttcc.library.exception.ConflictException;
import com.bbttcc.library.exception.ResourceNotFoundException;
import com.bbttcc.library.mapper.BookMapper;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.BorrowRecordRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * 图书服务。
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private static final EnumSet<BorrowStatus> ACTIVE_STATUSES =
            EnumSet.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE);

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final CategoryService categoryService;

    /**
     * 组合条件分页查询：关键字（书名/作者/ISBN）、分类、是否仅看有库存。
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> search(String keyword, Long categoryId, Boolean availableOnly, Pageable pageable) {
        Specification<Book> spec = buildSpecification(keyword, categoryId, availableOnly);
        return bookRepository.findAll(spec, pageable).map(BookMapper::toResponse);
    }

    private Specification<Book> buildSpecification(String keyword, Long categoryId, Boolean availableOnly) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like),
                        cb.like(cb.lower(root.get("isbn")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (Boolean.TRUE.equals(availableOnly)) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional(readOnly = true)
    public BookResponse findById(Long id) {
        return BookMapper.toResponse(getEntity(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new ConflictException("ISBN 已存在: " + request.isbn());
        }
        Category category = resolveCategory(request.categoryId());
        Book book = Book.builder()
                .isbn(request.isbn())
                .title(request.title())
                .author(request.author())
                .publisher(request.publisher())
                .publishYear(request.publishYear())
                .category(category)
                .totalCopies(request.totalCopies())
                .availableCopies(request.totalCopies())
                .description(request.description())
                .build();
        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = getEntity(id);

        bookRepository.findByIsbn(request.isbn())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("ISBN 已存在: " + request.isbn());
                });

        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        int newAvailable = request.totalCopies() - borrowed;
        if (newAvailable < 0) {
            throw new BusinessException("馆藏总量不能小于当前已借出数量(" + borrowed + ")");
        }

        book.setIsbn(request.isbn());
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setPublishYear(request.publishYear());
        book.setCategory(resolveCategory(request.categoryId()));
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(newAvailable);
        book.setDescription(request.description());
        return BookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = getEntity(id);
        long activeBorrows = borrowRecordRepository.countByBookIdAndStatusIn(id, ACTIVE_STATUSES);
        if (activeBorrows > 0) {
            throw new ConflictException("该图书仍有 " + activeBorrows + " 条未归还的借阅记录，无法删除");
        }
        bookRepository.delete(book);
    }

    /** 供借阅服务复用的实体查询。 */
    @Transactional(readOnly = true)
    public Book getEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("图书", id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryId == null ? null : categoryService.getEntity(categoryId);
    }
}
