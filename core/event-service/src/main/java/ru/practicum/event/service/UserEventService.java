package ru.practicum.event.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.api.event.UpdateEventUserRequest;

import java.util.List;

public interface UserEventService {

    EventFullDto addEvent(Long userId, NewEventDto event) throws ValidationException, WrongDataException, NotFoundException, ConflictException;

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest event) throws ConflictException, NotFoundException, ValidationException, WrongDataException;

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer count) throws NotFoundException;

    EventFullDto getEventById(Long userId, Long eventId) throws NotFoundException, ValidationException;
}
