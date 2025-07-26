package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.List;

public interface EventService {
    EventFullDto getEventById(Long eventId, String uri, String ip) throws NotFoundException;

    List<EventShortDto> getFilteredEvents(String text,
                                          List<Long> categories,
                                          Boolean paid,
                                          String rangeStart,
                                          String rangeEnd,
                                          Boolean onlyAvailable,
                                          String sort,
                                          Integer from,
                                          Integer size,
                                          String uri,
                                          String ip) throws ValidationException;
}
