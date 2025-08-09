package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.request.EventRequestInterface;
import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.service.EventRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController implements EventRequestInterface {

    private final EventRequestService requestService;

    @Override
    public EventRequestDto addEventRequest(Long userId, Long eventId) throws ConflictException, NotFoundException {
        return requestService.addRequest(userId, eventId);
    }

    @Override
    public List<EventRequestDto> getUserRequests(Long userId) throws NotFoundException {
        return requestService.getUserRequests(userId);
    }

    @Override
    public List<EventRequestDto> getRequestsByEventId(Long userId, Long eventId)
            throws ValidationException, NotFoundException {
        return requestService.getRequestsByEventId(userId, eventId);
    }

    @Override
    public EventRequestDto updateRequest(Long userId, Long eventId, EventRequestDto request)
            throws ValidationException, ConflictException, NotFoundException {
        return requestService.updateRequest(userId, eventId, request);
    }

    @Override
    public EventRequestDto cancelRequest(Long userId, Long requestId) throws ValidationException, NotFoundException {
        return requestService.cancelRequest(userId, requestId);
    }

    @Override
    public Long countByEventAndStatuses(Long eventId, List<String> statuses) {
        return requestService.countByEventAndStatuses(eventId, statuses);
    }

    @Override
    public List<EventRequestDto> getByEventAndStatus(List<Long> eventId, String status) {
        return requestService.getByEventAndStatus(eventId, status);
    }

    @Override
    public List<EventRequestDto> findByEventIds(List<Long> id) {
        return requestService.findByEventIds(id);
    }

    @Override
    public boolean isUserTakePart(Long userId, Long eventId) {
        return requestService.ifUserTakePart(userId, eventId);
    }
}
