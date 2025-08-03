package ru.practicum.api.category;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Set;

public interface CategoryInterface {
    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto addCategory(@Valid @RequestBody CategoryDto newCategory) throws ConflictException;

    @PatchMapping("/admin/categories/{catId}")
    CategoryDto updateCategory(@PathVariable Long catId,
                               @Valid @RequestBody CategoryDto categoryDto) throws ConflictException, NotFoundException;

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    void deleteCategory(@PathVariable Long catId) throws ConflictException, NotFoundException;

    @GetMapping("/inner/category/exist/{categoryId}")
    boolean existById(@PathVariable Long categoryId);

    @GetMapping("/inner/category/all")
    List<CategoryDto> getCategoriesByIds(@RequestParam Set<Long> ids);

    @GetMapping("/categories")
    List<CategoryDto> getAllCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
                                       @RequestParam(required = false, defaultValue = "10") Integer size);

    @GetMapping("/categories/{catId}")
    CategoryDto getCategoryById(@PathVariable Long catId) throws NotFoundException;
}
