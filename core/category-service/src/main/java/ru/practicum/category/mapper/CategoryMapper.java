package ru.practicum.category.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.category.model.Category;

@UtilityClass
public class CategoryMapper {
    public static Category mapCategoryDto(CategoryDto categoryDto) {
        return new Category(null, categoryDto.getName());
    }

    public static CategoryDto mapCategory(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

}
