package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.util.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEventServiceImpl implements UserEventService {

    final EventRepository eventRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;
    final RequestRepository requestRepository;
    final LocationRepository locationRepository;

    final StatsClient statsClient;

    private static void validationEventDate(Event event) throws ValidationException, WrongDataException {
        if (LocalDateTime.now().isAfter(event.getEventDate().minusHours(1))) {
            throw new ValidationException("До начала события меньше часа, изменение невозможно");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new WrongDataException("Событие уже завершилось");
        }
    }

    private static void validationEventInitiator(User user, Event event) throws ValidationException {
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Пользователь " + user.getId() + " не инициатор события " + event.getId());
        }
    }

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto eventDto) throws ValidationException, WrongDataException, NotFoundException {
        log.info("Users...");
        log.info("Добавление нового события пользователем {}", userId);
        User user = getUserById(userId);
        Category category = categoryRepository.findById(eventDto.getCategory()).orElseThrow(
                () -> new NotFoundException("Категория не найдена " + eventDto.getCategory())
        );

        Event event = EventMapper.mapNewEventDtoToEvent(eventDto, category);

        locationRepository.save(event.getLocation());

        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Long confirmedRequests = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));

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

        return EventMapper.mapEventToFullDto(event, confirmedRequests);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto) throws ConflictException, NotFoundException, ValidationException, WrongDataException {
        log.info("Users...");
        log.info("Редактирование данных события и его статуса");
        Event event = getEventById(eventId);
        User user = getUserById(userId);
        validationEventInitiator(user, event);
        validationEventDate(event);

        if ((!StateAction.REJECT_EVENT.toString().equals(eventDto.getStateAction())
                && event.getState().equals(EventState.PUBLISHED))) {
            throw new ConflictException("Отклонить опубликованное событие невозможно");
        }

        updateEventFromEventDto(event, eventDto);
        locationRepository.save(event.getLocation());
        eventRepository.save(event);
        Long confirmed = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        return getViewsCounter(EventMapper.mapEventToFullDto(event, confirmed));
    }

    private User getUserById(Long userId) throws NotFoundException {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer count) throws NotFoundException {
        User user = getUserById(userId);
        return eventRepository.findAllByInitiator(user, PageRequest.of(from / count, count)).stream()
                .map(EventMapper::mapEventToShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) throws NotFoundException, ValidationException {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Пользователь " + userId + " не является инициатором события " + eventId);
        }
        Long confirmed = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED"));
        return getViewsCounter(EventMapper.mapEventToFullDto(event, confirmed));
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
            Category category = categoryRepository.findById(inpEventDto.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория не найдена " + inpEventDto.getCategory()));
            event.setCategory(category);
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
            event.setLocation(inpEventDto.getLocation());
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

    EventFullDto getViewsCounter(EventFullDto eventFullDto) {
        ArrayList<String> urls = new ArrayList<>(List.of("/events/" + eventFullDto.getId()));
        LocalDateTime start = LocalDateTime.parse(eventFullDto.getCreatedOn(), DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
        LocalDateTime end = LocalDateTime.now();

        Integer views = statsClient.getAllStats(start, end, urls, true).size();
        eventFullDto.setViews(views);
        return eventFullDto;
    }
}
