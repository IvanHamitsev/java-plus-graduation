package ru.practicum.compilation.service;

import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ResponseCompilationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.api.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    ResponseCompilationDto addCompilation(NewCompilationDto dto) throws NotFoundException;

    ResponseCompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilation) throws NotFoundException;

    ResponseCompilationDto getCompilationById(Long id) throws NotFoundException;

    List<ResponseCompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    void deleteCompilation(Long id) throws ValidationException, NotFoundException;
}
