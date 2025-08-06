package ru.practicum.event.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.api.event.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) throws ValidationException;

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest event) throws ConflictException, ValidationException, NotFoundException, WrongDataException;

}
