package ru.practicum.api.category;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "category-service")
public interface CategoryClient extends CategoryInterface {
}
