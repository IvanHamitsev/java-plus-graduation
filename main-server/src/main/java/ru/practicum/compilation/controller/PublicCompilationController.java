package ru.practicum.compilation.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicCompilationController {

    final CompilationService compilationService;

    @GetMapping
    public List<ResponseCompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public ResponseCompilationDto getCompilationById(@PathVariable Long compId) throws NotFoundException {
        return compilationService.getCompilationById(compId);
    }


}
