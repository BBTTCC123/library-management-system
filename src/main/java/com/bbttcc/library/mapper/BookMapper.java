package com.bbttcc.library.mapper;

import com.bbttcc.library.dto.book.BookResponse;
import com.bbttcc.library.entity.Book;
import com.bbttcc.library.entity.Category;

public final class BookMapper {

    private BookMapper() {
    }

    public static BookResponse toResponse(Book book) {
        Category category = book.getCategory();
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishYear(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getDescription(),
                book.getCreatedAt()
        );
    }
}
