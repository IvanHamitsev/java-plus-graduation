package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;

import java.util.List;

public interface UserEventService {

    EventFullDto addEvent(Long userId, NewEventDto event) throws ValidationException, WrongDataException, NotFoundException;

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest event) throws ConflictException, NotFoundException, ValidationException, WrongDataException;

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer count) throws NotFoundException;

    EventFullDto getEventById(Long userId, Long eventId) throws NotFoundException, ValidationException;
}
