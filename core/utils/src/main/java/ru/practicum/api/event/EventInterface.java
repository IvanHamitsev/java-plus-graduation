package ru.practicum.api.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

public interface EventInterface {

    @GetMapping("/events/{id}")
    EventFullDto getEventById(@PathVariable Long id, HttpServletRequest request) throws NotFoundException;

    @GetMapping("/events")
    List<EventShortDto> getFilteredEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean available,
            @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer count,
            HttpServletRequest request
    ) throws ValidationException;

    @GetMapping("/admin/events")
    List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(required = false) LocalDateTime rangeStart,
            @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "0") Integer from,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) throws ValidationException;

    @PatchMapping("/admin/events/{eventId}")
    EventFullDto updateEvent(@PathVariable Long eventId,
                             @Valid @RequestBody UpdateEventAdminRequest event) throws ValidationException,
            ConflictException, WrongDataException, NotFoundException;

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer count
    ) throws NotFoundException;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/events")
    EventFullDto addEvent(
            @PathVariable Long userId,
            @Valid @RequestBody NewEventDto event
    ) throws ValidationException, WrongDataException, NotFoundException, ConflictException;

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getUserEventById(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) throws ValidationException, NotFoundException;

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto updateUserEvent(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest event
    ) throws ValidationException, ConflictException, WrongDataException, NotFoundException;

    // выделить в отдельный интерфейс?

    @GetMapping("/inner/event/{eventId}")
    EventFullDto getInnerEventById(@PathVariable Long eventId) throws NotFoundException;

    @GetMapping("/inner/event/{eventId}/exist")
    boolean existsById(@PathVariable Long eventId);

    @GetMapping("/inner/event/category/{categoryId}/exist")
    boolean existsByCategoryId(@PathVariable Long categoryId);

    @GetMapping("/inner/event/short/ids")
    List<EventShortDto> getShortByIds(@RequestParam List<Long> ids);
}
