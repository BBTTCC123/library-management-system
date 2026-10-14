package com.bbttcc.library.dto.book;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "图书信息")
public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        Integer publishYear,
        Long categoryId,
        String categoryName,
        int totalCopies,
        int availableCopies,
        String description,
        Instant createdAt
) {
}
