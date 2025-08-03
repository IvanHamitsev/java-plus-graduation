package ru.practicum.event.service;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
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

    EventFullDto getEventById(Long eventId) throws NotFoundException;

    boolean existsById(Long eventId);

    boolean existsByCategoryId(Long categoryId);

    List<EventShortDto> getShortByIds(List<Long> ids);
}
