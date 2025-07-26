package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.ResponseCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCompilationController {

    final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCompilationDto add(@Valid @RequestBody NewCompilationDto compilationDto) {
        return compilationService.addCompilation(compilationDto);
    }

    @PatchMapping("/{compId}")
    public ResponseCompilationDto update(@PathVariable Long compId,
                                         @Valid @RequestBody UpdateCompilationRequest compilationDto) throws NotFoundException {
        return compilationService.updateCompilation(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) throws ValidationException, NotFoundException {
        compilationService.deleteCompilation(compId);
    }
}
