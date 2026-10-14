package com.bbttcc.library.service;

import com.bbttcc.library.dto.category.CategoryRequest;
import com.bbttcc.library.dto.category.CategoryResponse;
import com.bbttcc.library.entity.Category;
import com.bbttcc.library.exception.ConflictException;
import com.bbttcc.library.exception.ResourceNotFoundException;
import com.bbttcc.library.mapper.CategoryMapper;
import com.bbttcc.library.repository.BookRepository;
import com.bbttcc.library.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 图书分类服务。
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return CategoryMapper.toResponse(getEntity(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("分类已存在: " + request.name());
        }
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        categoryRepository.findByName(request.name())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("分类已存在: " + request.name());
                });
        category.setName(request.name());
        category.setDescription(request.description());
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getEntity(id);
        long bookCount = bookRepository.countByCategoryId(id);
        if (bookCount > 0) {
            throw new ConflictException("该分类下仍有 " + bookCount + " 本图书，无法删除");
        }
        categoryRepository.delete(category);
    }

    /** 供其他服务复用的实体查询。 */
    @Transactional(readOnly = true)
    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("分类", id));
    }
}
