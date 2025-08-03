package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.compilation.PublicCompilationInterface;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.dto.compilation.ResponseCompilationDto;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicCompilationController implements PublicCompilationInterface {

    private final CompilationService compilationService;

    @Override
    public List<ResponseCompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @Override
    public ResponseCompilationDto getCompilationById(Long compId) throws NotFoundException {
        return compilationService.getCompilationById(compId);
    }


}
