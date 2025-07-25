package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestDto;
import ru.practicum.request.dto.EventRequestMapper;
import ru.practicum.request.model.EventRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.request.model.EventRequestStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestServiceImpl implements EventRequestService {

    final EventRepository eventRepository;
    final EventRequestMapper eventRequestMapper;
    final UserRepository userRepository;
    final RequestRepository requestRepository;

    @Override
    @Transactional
    public EventRequestDto addRequest(Long userId, Long eventId) throws ConflictException, NotFoundException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Создатель события не может подать заявку на участие");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие не опубликовано");
        }
        List<EventRequest> requests = getEventRequestsByEventId(event.getId());
        if (participationLimitIsFull(event)) {
            throw new ConflictException("Превышен лимит заявок на участие в событии");
        }
        for (EventRequest request : requests) {
            if (request.getRequester().getId().equals(userId)) {
                throw new ConflictException("Повторная заявка на участие в событии");
            }
        }

        EventRequest newRequest = createNewEventRequest(user, event);
        return eventRequestMapper.mapRequest(requestRepository.save(newRequest));
    }

    @Override
    public List<EventRequestDto> getUserRequests(Long userId) throws NotFoundException {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден userId=" + userId);
        }
        return requestRepository.findByUserId(userId).stream()
                .map(eventRequestMapper::mapRequest)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventRequestDto> getRequestsByEventId(Long userId, Long eventId) throws ValidationException, NotFoundException {
        List<EventRequest> requests = getEventRequests(userId, eventId);
        return requests.stream()
                .map(eventRequestMapper::mapRequest)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestDto updateRequest(Long userId,
                                         Long eventId,
                                         EventRequestDto updateRequest) throws ConflictException, ValidationException, NotFoundException {
        Event event = getEventById(eventId);
        List<EventRequest> requests = getEventRequestsByEventId(eventId);
        long confirmedRequestsCounter = requests.stream().filter(r -> r.getStatus().equals(CONFIRMED_REQUEST)).count();

        List<EventRequestDto> confirmedRequests = new ArrayList<>();
        List<EventRequestDto> rejectedRequests = new ArrayList<>();

        List<EventRequest> result = new ArrayList<>();

        List<EventRequest> pending = requests.stream()
                .filter(p -> p.getStatus().equals(PENDING_REQUEST)).collect(Collectors.toList());


        for (EventRequest request : requests) {
            if (request.getStatus().equals(CONFIRMED_REQUEST) ||
                    request.getStatus().equals(REJECTED_REQUEST) ||
                    request.getStatus().equals(PENDING_REQUEST)) {

                if (updateRequest.getStatus().equals(CONFIRMED_REQUEST) && event.getParticipantLimit() != 0) {
                    if (event.getParticipantLimit() < confirmedRequestsCounter) {

                        pending.stream().peek(p -> p.setStatus(REJECTED_REQUEST)).collect(Collectors.toList());

                        throw new ConflictException("Превышено число возможных заявок на участие");
                    }
                }

                if (updateRequest.getStatus().equals(REJECTED_REQUEST) && request.getStatus().equals(CONFIRMED_REQUEST)) {
                    throw new ConflictException("Нельзя отменить подтверждённую заявку");
                }

                request.setStatus(updateRequest.getStatus());
                EventRequestDto participationRequestDto = eventRequestMapper.mapRequest(request);

                if ("CONFIRMED".equals(participationRequestDto.getStatus())) {
                    confirmedRequests.add(participationRequestDto);
                } else if ("REJECTED".equals(participationRequestDto.getStatus())) {
                    rejectedRequests.add(participationRequestDto);
                }

                result.add(request);
                confirmedRequestsCounter++;

            } else {
                throw new ValidationException("Неверный статус заявки");
            }
        }

        requestRepository.saveAll(pending);

        requestRepository.saveAll(result);

        return eventRequestMapper.mapRequestWithConfirmedAndRejected(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional
    public EventRequestDto cancelRequest(Long userId, Long requestId) throws NotFoundException, ValidationException {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден userId=" + userId);
        }

        EventRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос не существует")
        );
        if (!request.getRequester().getId().equals(userId)) {
            throw new ValidationException("Создатель заявки не userId=" + userId);
        }
        request.setStatus(CANCELED_REQUEST);
        return eventRequestMapper.mapRequest(requestRepository.save(request));
    }

    private EventRequest createNewEventRequest(User user, Event event) {
        EventRequest newRequest = new EventRequest();
        newRequest.setRequester(user);
        newRequest.setCreated(LocalDateTime.now());
        if (event.getParticipantLimit() == 0) {
            newRequest.setStatus(CONFIRMED_REQUEST);
        } else {
            newRequest.setStatus(PENDING_REQUEST);
        }
        newRequest.setEvent(event);
        if (!event.getRequestModeration()) {
            newRequest.setStatus(ACCEPTED_REQUEST);
        }
        return newRequest;
    }

    private boolean participationLimitIsFull(Event event) throws ConflictException {
        Long confirmedRequestsCounter = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED", "ACCEPTED"));
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequestsCounter) {
            throw new ConflictException("Превышено число заявок на участие");
        }
        return false;
    }

    private List<EventRequest> getEventRequests(Long userId, Long eventId) throws ValidationException, NotFoundException {
        User user = userRepository.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("Пользователь не инициатор события c id=" + eventId);
        }
        return requestRepository.findByEventInitiatorId(userId);
    }

    private List<EventRequest> getEventRequestsByEventId(Long eventId) throws NotFoundException {
        if (eventRepository.existsById(eventId)) {
            return requestRepository.findByEventId(eventId);
        } else {
            throw new NotFoundException("Событие не найдено eventId=" + eventId);
        }
    }

    private Event getEventById(Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не найдено eventId=" + eventId));
    }
}
