package ru.practicum.event.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.util.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@UtilityClass
public class EventMapper {

    public static EventFullDto mapEventToFullDto(Event event, Long confirmed) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.mapCategory(event.getCategory()));
        eventFullDto.setConfirmedRequests(confirmed);
        eventFullDto.setCreatedOn(getLocalDateTime(event.getCreatedOn()));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(getLocalDateTime(event.getEventDate()));
        eventFullDto.setInitiator(UserMapper.mapUser(event.getInitiator()));
        eventFullDto.setLocation(event.getLocation());
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(getLocalDateTime(event.getPublishedOn()));
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState() == null ? EventState.PENDING : event.getState());
        eventFullDto.setTitle(event.getTitle());
        return eventFullDto;
    }

    public static EventShortDto mapEventToShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setCategory(CategoryMapper.mapCategory(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.mapUser(event.getInitiator()));
        eventShortDto.setId(event.getId());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setEventDate(event.getEventDate());
        return eventShortDto;
    }

    public static Event mapNewEventDtoToEvent(NewEventDto newEvent, Category category) {
        Event event = new Event();
        event.setAnnotation(newEvent.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEvent.getDescription());
        event.setEventDate(getFromString(newEvent.getEventDate()));
        event.setLocation(newEvent.getLocation());
        event.setPaid(newEvent.getPaid());
        event.setParticipantLimit(newEvent.getParticipantLimit());
        event.setRequestModeration(newEvent.getRequestModeration());
        event.setTitle(newEvent.getTitle());
        return event;
    }

    static String getLocalDateTime(LocalDateTime time) {
        if (time == null) {
            return null;
        }
        return time.format(DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
    }

    static LocalDateTime getFromString(String time) {
        if (time == null) {
            return null;
        }
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(JSON_FORMAT_PATTERN_FOR_TIME));
    }
}
