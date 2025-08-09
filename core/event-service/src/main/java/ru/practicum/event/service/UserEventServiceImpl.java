package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.api.category.CategoryClient;
import ru.practicum.api.event.UpdateEventUserRequest;
import ru.practicum.api.request.EventRequestClient;
import ru.practicum.api.user.UserClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEventServiceImpl implements UserEventService {
    final EventRepository eventRepository;
    final LocationRepository locationRepository;

    final UserClient userClient;
    final CategoryClient categoryClient;
    final EventRequestClient requestClient;
    final AnalyzerClient analyzerClient;

    private static void validationEventDate(Event event) throws ValidationException, WrongDataException {
        if (LocalDateTime.now().isAfter(event.getEventDate().minusHours(1))) {
            throw new ValidationException("До начала события меньше часа, изменение невозможно");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new WrongDataException("Событие уже завершилось");
        }
    }

    private static void validationEventInitiator(UserDto user, Event event) throws ValidationException {
        if (!user.getId().equals(event.getInitiator())) {
            throw new ValidationException("Пользователь " + user.getId() + " не инициатор события " + event.getId());
        }
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto eventDto) throws ValidationException, WrongDataException, NotFoundException, ConflictException {
        log.info("Users...");
        log.info("Добавление нового события пользователем {}", userId);
        UserDto user = getUserById(userId);
        CategoryDto category = categoryClient.getCategoryById(eventDto.getCategory());

        Event event = EventMapper.mapNewEventDtoToEvent(eventDto, category);

        locationRepository.save(event.getLocation());

        event.setInitiator(user.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        validationEventDate(event);
        event = eventRepository.save(event);
        log.info("Событие сохранено {}", event.getId());

        return EventMapper.mapEventToFullDto(event, 0L, category, user);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) throws ConflictException, NotFoundException, ValidationException, WrongDataException {
        log.info("Users...");
        log.info("Редактирование данных события и его статуса");
        Event event = getEventById(eventId);
        UserDto user = getUserById(userId);
        validationEventInitiator(user, event);
        validationEventDate(event);

        if ((!StateAction.REJECT_EVENT.toString().equals(eventDto.getStateAction())
                && event.getState().equals(EventState.PUBLISHED))) {
            throw new ConflictException("Отклонить опубликованное событие невозможно");
        }

        updateEventFromEventDto(event, eventDto);
        locationRepository.save(event.getLocation());
        eventRepository.save(event);
        Long confirmed = requestClient.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        return getRatingCounter(EventMapper.mapEventToFullDto(event, confirmed,
                categoryClient.getCategoryById(event.getCategory()), user));
    }

    private UserDto getUserById(Long userId) throws NotFoundException {
        return userClient.getById(userId);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer count) throws NotFoundException {
        UserDto user = getUserById(userId);
        List<Event> allEvents = eventRepository.findAllByInitiator(user.getId(), PageRequest.of(from / count, count));
        Set<Long> categoriesIds = allEvents.stream().map(Event::getCategory).collect(Collectors.toSet());
        Map<Long, CategoryDto> categories = categoryClient.getCategoriesByIds(categoriesIds).stream()
                .collect(Collectors.toMap(CategoryDto::getId, c -> c));
        return allEvents.stream()
                .map(e -> EventMapper.mapEventToShortDto(e, categories.get(e.getCategory()), user))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) throws NotFoundException, ValidationException {
        UserDto user = getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator())) {
            throw new ValidationException("Пользователь " + userId + " не является инициатором события " + eventId);
        }
        Long confirmed = requestClient.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        return getRatingCounter(EventMapper.mapEventToFullDto(event, confirmed,
                categoryClient.getCategoryById(event.getCategory()), user));
    }

    // Вспомогательные функции

    Event getEventById(Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие " + eventId + " не найдено"));
    }

    void updateEventFromEventDto(Event event, UpdateEventUserRequest inpEventDto) throws NotFoundException, ValidationException {
        if (inpEventDto.getAnnotation() != null) {
            event.setAnnotation(inpEventDto.getAnnotation());
        }
        if (inpEventDto.getCategory() != null) {
            if (!categoryClient.existById(inpEventDto.getCategory())) {
                throw new NotFoundException("Категория не найдена " + inpEventDto.getCategory());
            }
            event.setCategory(inpEventDto.getCategory());
        }
        if (inpEventDto.getDescription() != null) {
            event.setDescription(inpEventDto.getDescription());
        }
        if (inpEventDto.getEventDate() != null) {
            LocalDateTime updateEventDate = LocalDateTime.parse(inpEventDto.getEventDate(),
                    DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
            if (LocalDateTime.now().isAfter(updateEventDate)) {
                throw new ValidationException("Нельзя установить дату из прошлого.");
            }
            event.setEventDate(updateEventDate);
        }
        if (inpEventDto.getLocation() != null) {
            event.setLocation(EventMapper.mapDtoToLocation(inpEventDto.getLocation()));
        }
        if (inpEventDto.getPaid() != null) {
            event.setPaid(inpEventDto.getPaid());
        }
        if (inpEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(inpEventDto.getParticipantLimit());
        }
        if (inpEventDto.getRequestModeration() != null) {
            event.setRequestModeration(inpEventDto.getRequestModeration());
        }
        if (inpEventDto.getStateAction() != null) {
            switch (inpEventDto.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new ValidationException("Неверное состояние события, не удалось обновить");
            }
        }
    }

    EventFullDto getRatingCounter(EventFullDto eventFullDto) {
        List<RecommendedEventProto> recommendedEventProtoList = analyzerClient.getInteractionsCount(
                InteractionsCountRequestProto.newBuilder().addEventId(eventFullDto.getId()).build()
        );
        Double rating = recommendedEventProtoList.isEmpty() ? 0.0 : recommendedEventProtoList.getFirst().getScore();
        eventFullDto.setRating(rating);
        return eventFullDto;
    }
}
