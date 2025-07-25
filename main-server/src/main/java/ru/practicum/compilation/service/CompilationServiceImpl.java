package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationMapper;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final EventRepository eventRepository;

    @Override
    @Transactional
    public ResponseCompilationDto addCompilation(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.mapToCompilation(dto);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        List<Event> events = getEventsFromDto(dto);
        compilation.setEvents(events);
        ResponseCompilationDto responseCompilationDto = CompilationMapper.mapToResponseCompilation(
                compilationRepository.save(compilation)
        );
        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Event event : compilation.getEvents()) {
            eventDtos.add(EventMapper.mapEventToShortDto(event));
        }
        responseCompilationDto.setEvents(eventDtos);

        return responseCompilationDto;
    }

    @Override
    public ResponseCompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilation) throws NotFoundException {
        Compilation old = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Указанная подборка не найдена " + compId));

        Compilation update = new Compilation();
        update.setId(compId);
        update.setPinned(compilation.getPinned() == null ? old.getPinned() : compilation.getPinned());
        update.setTitle(compilation.getTitle() == null ? old.getTitle() : compilation.getTitle());

        List<Event> events = getEventsFromDto(compilation);
        update.setEvents(events == null ? old.getEvents() : events);

        return CompilationMapper.mapToResponseCompilation(compilationRepository.save(update));
    }

    private List<Event> getEventsFromDto(NewCompilationDto compilation) {
        List<Event> events = Collections.emptyList();
        if (compilation.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilation.getEvents());
            log.info("EventIDS: {}", events.toString());
        }
        return events;
    }

    private List<Event> getEventsFromDto(UpdateCompilationRequest compilation) {
        List<Event> events = new ArrayList<>();
        if (compilation.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilation.getEvents());
        }
        return events;
    }

    @Override
    public ResponseCompilationDto getCompilationById(Long id) throws NotFoundException {
        log.info("Получение информации о подборке, id={}", id);
        Compilation compilation = compilationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Подборка не найдена " + id)
        );
        return compileDtoWithEvents(compilation);
    }

    @Override
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("pinned {}", pinned);
        List<Compilation> allWithPinned = compilationRepository.findAllWithPinned(pinned, Pageable.ofSize(size + from));
        return compileDtosWithEvents(allWithPinned);
    }

    @Override
    public void deleteCompilation(Long id) throws ValidationException, NotFoundException {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Указанная категория не найдена " + id));
        try {
            compilationRepository.delete(compilation);
        } catch (Exception e) {
            throw new ValidationException("Невозможно удаление используемой категории события " + e.getMessage());
        }
    }

    private List<ResponseCompilationDto> compileDtosWithEvents(List<Compilation> compilations) {

        return compilations.stream()
                .map(this::compileDtoWithEvents)
                .collect(Collectors.toList());
    }

    private ResponseCompilationDto compileDtoWithEvents(Compilation compilation) {
        ResponseCompilationDto result = CompilationMapper.mapToResponseCompilation(compilation);
        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Event event : compilation.getEvents()) {
            eventDtos.add(EventMapper.mapEventToShortDto(event));
        }
        result.setEvents(eventDtos);
        return result;
    }
}
