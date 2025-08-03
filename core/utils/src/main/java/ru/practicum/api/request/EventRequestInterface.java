package ru.practicum.api.request;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;

public interface EventRequestInterface {
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    EventRequestDto addEventRequest(
            @PathVariable Long userId,
            @RequestParam Long eventId
    ) throws ConflictException, NotFoundException;

    @GetMapping("/users/{userId}/requests")
    List<EventRequestDto> getUserRequests(@PathVariable Long userId) throws NotFoundException;

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    List<EventRequestDto> getRequestsByEventId(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) throws ValidationException, NotFoundException;

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    EventRequestDto updateRequest(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestDto request
    ) throws ValidationException, ConflictException, NotFoundException;

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    EventRequestDto cancelRequest(
            @PathVariable Long userId,
            @PathVariable Long requestId
    ) throws ValidationException, NotFoundException;

    @GetMapping("/inner/request/{eventId}/status/count")
    Long countByEventAndStatuses(@PathVariable Long eventId, @RequestParam List<String> statuses);

    @GetMapping("/inner/request/events/{status}")
    List<EventRequestDto> getByEventAndStatus(@RequestParam List<Long> eventId, @PathVariable String status);

    @GetMapping("/inner/request/events")
    List<EventRequestDto> findByEventIds(@RequestParam List<Long> id);
}
