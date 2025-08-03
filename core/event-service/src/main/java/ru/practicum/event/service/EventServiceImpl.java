package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.api.category.CategoryClient;
import ru.practicum.api.request.EventRequestClient;
import ru.practicum.api.user.UserClient;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {
    final EventRepository eventRepository;

    final UserClient userClient;
    final CategoryClient categoryClient;
    final EventRequestClient requestClient;
    final StatsClient statsClient;

    @Override
    public EventFullDto getEventById(Long eventId, String uri, String ip) throws NotFoundException {
        statsClient.postStats(new StatsRequestDto("main-server",
                uri,
                ip,
                LocalDateTime.now()));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED) && !uri.toLowerCase().contains("admin")) {
            throw new NotFoundException("Такого события не существует");
        }
        var confirmed = requestClient.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        EventFullDto eventFullDto = EventMapper.mapEventToFullDto(
                event,
                confirmed,
                categoryClient.getCategoryById(event.getCategory()),
                userClient.getById(event.getInitiator())
        );

        List<String> urls = Collections.singletonList(uri);
        LocalDateTime start = LocalDateTime.parse(eventFullDto.getCreatedOn(), DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
        LocalDateTime end = LocalDateTime.now();
        var views = statsClient.getAllStats(start, end, urls, true).size();
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getFilteredEvents(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 Boolean onlyAvailable,
                                                 String sort,
                                                 Integer from,
                                                 Integer size,
                                                 String uri,
                                                 String ip) throws ValidationException {
        List<Event> events;
        LocalDateTime startDate;
        LocalDateTime endDate;
        boolean sortDate = sort.equals("EVENT_DATE");
        if (sortDate) {
            if (rangeStart == null && rangeEnd == null && categories != null) {
                //events = eventRepository.findAllByCategoryIdPageable(categories, EventState.PUBLISHED, PageRequest.of(from / size, size, Sort.Direction.DESC));
                events = eventRepository.findAllByCategoryIdPageable(categories, EventState.PUBLISHED,
                        PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "e.eventDate")));
            } else {
                if (rangeStart == null) {
                    startDate = LocalDateTime.now();
                } else {
                    startDate = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
                }
                if (text == null) {
                    text = "";
                }
                if (rangeEnd == null) {
                    events = eventRepository.findEventsByText("%" + text.toLowerCase() + "%", EventState.PUBLISHED,
                            PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "e.eventDate")));
                } else {
                    endDate = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
                    if (startDate.isAfter(endDate)) {
                        throw new ValidationException("Дата и время начала поиска не должна быть позже даты и времени конца поиска");
                    } else {
                        events = eventRepository.findAllByTextAndDateRange("%" + text.toLowerCase() + "%",
                                startDate,
                                endDate,
                                EventState.PUBLISHED,
                                PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "e.eventDate")));
                    }
                }
            }
        } else {
            if (rangeStart == null) {
                startDate = LocalDateTime.now();
            } else {
                startDate = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
            }
            if (rangeEnd == null) {
                endDate = null;
            } else {
                endDate = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
            }
            if (rangeStart != null && rangeEnd != null) {
                if (startDate.isAfter(endDate)) {
                    throw new ValidationException("Дата и время начала поиска не должна быть позже даты и времени конца поиска");
                }
            }
            events = eventRepository.findEventList(text, categories, paid, startDate, endDate, EventState.PUBLISHED);
        }
        statsClient.postStats(new StatsRequestDto("main-server",
                uri,
                ip,
                LocalDateTime.now()));
        if (!sortDate) {
            List<EventShortDto> shortEventDtos = createShortEventDtos(events);
            shortEventDtos.sort(Comparator.comparing(EventShortDto::getViews));
            shortEventDtos = shortEventDtos.subList(from, Math.min(from + size, shortEventDtos.size()));
            return shortEventDtos;
        }
        return createShortEventDtos(events);
    }

    @Override
    public EventFullDto getEventById(Long eventId) throws NotFoundException {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Не найдено событие " + eventId)
        );
        return EventMapper.mapEventToFullDto(event, null, categoryClient.getCategoryById(event.getCategory()),
                userClient.getById(event.getInitiator()));
    }

    @Override
    public boolean existsById(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategory(categoryId);
    }

    @Override
    public List<EventShortDto> getShortByIds(List<Long> ids) {
        List<Event> events = eventRepository.findAllById(ids);
        List<Long> usersIds = events.stream().map(Event::getInitiator).toList();
        Set<Long> categoriesIds = events.stream().map(Event::getCategory).collect(Collectors.toSet());
        Map<Long, UserDto> users = userClient.getUsersList(usersIds, 0, Math.max(events.size(), 1)).stream()
                .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
        Map<Long, CategoryDto> categories = categoryClient.getCategoriesByIds(categoriesIds).stream()
                .collect(Collectors.toMap(CategoryDto::getId, categoryDto -> categoryDto));

        return events.stream().map(
                e -> EventMapper.mapEventToShortDto(e, categories.get(e.getCategory()), users.get(e.getInitiator()))
        ).collect(Collectors.toList());
    }

    List<EventShortDto> createShortEventDtos(List<Event> events) {
        HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();

        LocalDateTime startTime = events.getFirst().getCreatedOn();
        ArrayList<String> uris = new ArrayList<>();
        for (Event event : events) {
            uris.add("/events/" + event.getId().toString());
            if (startTime.isAfter(event.getCreatedOn())) {
                startTime = event.getCreatedOn();
            }
        }

        var viewsCounter = statsClient.getAllStats(startTime, LocalDateTime.now(), uris, true);
        for (var statsDto : viewsCounter) {
            String[] split = statsDto.getUri().split("/");
            eventIdsWithViewsCounter.put(Long.parseLong(split[2]), Math.toIntExact(statsDto.getHits()));
        }
        List<EventRequestDto> requests = requestClient.findByEventIds(new ArrayList<>(eventIdsWithViewsCounter.keySet()));
        List<Long> usersIds = events.stream().map(Event::getInitiator).toList();
        Set<Long> categoriesIds = events.stream().map(Event::getCategory).collect(Collectors.toSet());
        Map<Long, UserDto> users = userClient.getUsersList(usersIds, 0, Math.max(events.size(), 1)).stream()
                .collect(Collectors.toMap(UserDto::getId, userDto -> userDto));
        Map<Long, CategoryDto> categories = categoryClient.getCategoriesByIds(categoriesIds).stream()
                .collect(Collectors.toMap(CategoryDto::getId, categoryDto -> categoryDto));
        return events.stream()
                .map(e -> EventMapper.mapEventToShortDto(e, categories.get(e.getCategory()), users.get(e.getInitiator())))
                .peek(dto -> dto.setConfirmedRequests(
                        requests.stream()
                                .filter((request -> request.getEvent().equals(dto.getId())))
                                .count()
                ))
                .peek(dto -> dto.setViews(eventIdsWithViewsCounter.get(dto.getId())))
                .collect(Collectors.toList());
    }
}
