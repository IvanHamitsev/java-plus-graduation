package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.category.CategoryInterface;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryInterface {
    private final CategoryService categoryService;

    @Override
    public CategoryDto addCategory(CategoryDto newCategory) throws ConflictException {
        return categoryService.addCategory(newCategory);
    }

    @Override
    public CategoryDto updateCategory(Long catId,
                                      CategoryDto categoryDto) throws ConflictException, NotFoundException {
        return categoryService.updateCategory(catId, categoryDto);
    }

    @Override
    public void deleteCategory(Long catId) throws ConflictException, NotFoundException {
        categoryService.deleteCategory(catId);
    }

    @Override
    public boolean existById(Long categoryId) {
        return categoryService.existById(categoryId);
    }

    @Override
    public List<CategoryDto> getCategoriesByIds(Set<Long> ids) {
        return categoryService.getCategoriesByIds(ids);
    }

    @Override
    public List<CategoryDto> getAllCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        return categoryService.getAllCategories(from, size);
    }

    @Override
    public CategoryDto getCategoryById(@PathVariable Long catId) throws NotFoundException {
        return categoryService.getCategoryById(catId);
    }
}
