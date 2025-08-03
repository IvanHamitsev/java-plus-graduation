package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.AdminCompilationInterface;
import ru.practicum.api.compilation.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ResponseCompilationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

@RestController
@RequiredArgsConstructor
public class AdminCompilationController implements AdminCompilationInterface {

    private final CompilationService compilationService;

    @Override
    public ResponseCompilationDto add(NewCompilationDto compilationDto) throws NotFoundException {
        return compilationService.addCompilation(compilationDto);
    }

    @Override
    public ResponseCompilationDto update(Long compId, UpdateCompilationRequest compilationDto) throws NotFoundException {
        return compilationService.updateCompilation(compId, compilationDto);
    }

    @Override
    public void delete(Long compId) throws ValidationException, NotFoundException {
        compilationService.deleteCompilation(compId);
    }
}
