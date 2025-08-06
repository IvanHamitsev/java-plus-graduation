package ru.practicum.api.compilation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ResponseCompilationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.api.compilation.UpdateCompilationRequest;

public interface AdminCompilationInterface {
    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseCompilationDto add(@Valid @RequestBody NewCompilationDto compilationDto) throws NotFoundException;

    @PatchMapping("/admin/compilations/{compId}")
    ResponseCompilationDto update(
            @PathVariable Long compId,
            @Valid @RequestBody UpdateCompilationRequest compilationDto
    ) throws NotFoundException;

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long compId) throws ValidationException, NotFoundException;
}
