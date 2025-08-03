package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventInterface;
import ru.practicum.api.event.UpdateEventAdminRequest;
import ru.practicum.api.event.UpdateEventUserRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.event.service.AdminEventService;
import ru.practicum.event.service.EventService;
import ru.practicum.event.service.UserEventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventController implements EventInterface {

    final EventService eventService;
    final AdminEventService adminEventService;
    final UserEventService userEventService;

    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) throws NotFoundException {
        return eventService.getEventById(id, request.getRequestURI(), request.getRemoteAddr());
    }

    @Override
    public List<EventShortDto> getFilteredEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean available,
            String sort,
            Integer from,
            Integer count,
            HttpServletRequest request
    ) throws ValidationException {
        return eventService.getFilteredEvents(text, categories, paid, rangeStart, rangeEnd, available, sort, from, count,
                request.getRequestURI(), request.getRemoteAddr());
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Integer from,
                                        Integer size) throws ValidationException {
        return adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    public EventFullDto updateEvent(Long eventId,
                                    UpdateEventAdminRequest event) throws ValidationException, ConflictException, WrongDataException, NotFoundException {
        return adminEventService.updateEvent(eventId, event);
    }

    @Override
    public boolean existsById(Long eventId) {
        return eventService.existsById(eventId);
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return eventService.existsByCategoryId(categoryId);
    }

    @Override
    public List<EventShortDto> getShortByIds(List<Long> ids) {
        return eventService.getShortByIds(ids);
    }

    @Override
    public EventFullDto getInnerEventById(Long eventId) throws NotFoundException {
        return eventService.getEventById(eventId);
    }

    @Override
    public List<EventShortDto> getUserEvents(
            Long userId,
            Integer from,
            Integer count
    ) throws NotFoundException {
        return userEventService.getUserEvents(userId, from, count);
    }

    @Override
    public EventFullDto addEvent(
            Long userId,
            NewEventDto event
    ) throws ValidationException, WrongDataException, NotFoundException, ConflictException {
        return userEventService.addEvent(userId, event);
    }

    @Override
    public EventFullDto getUserEventById(
            Long userId,
            Long eventId
    ) throws ValidationException, NotFoundException {
        return userEventService.getEventById(userId, eventId);
    }

    @Override
    public EventFullDto updateUserEvent(
            Long userId,
            Long eventId,
            UpdateEventUserRequest event
    ) throws ValidationException, ConflictException, WrongDataException, NotFoundException {
        return userEventService.updateEvent(userId, eventId, event);
    }
}