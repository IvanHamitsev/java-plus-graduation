package ru.practicum.api.event;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "event-service")
public interface EventClient extends EventInterface {
}
