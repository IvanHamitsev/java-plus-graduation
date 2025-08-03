package ru.practicum.api.request;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service")
public interface EventRequestClient extends EventRequestInterface {
}
