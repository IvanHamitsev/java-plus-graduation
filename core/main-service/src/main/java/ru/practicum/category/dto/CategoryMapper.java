package ru.practicum.category.dto;

import lombok.experimental.UtilityClass;
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
