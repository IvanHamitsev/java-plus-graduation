package ru.practicum.client;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public interface StatsClientInterface {
    static final String PATTERN_FOR_TIME = "yyyy-MM-dd HH:mm:ss";

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatsResponseDto> getAllStats(@DateTimeFormat(pattern = PATTERN_FOR_TIME) @RequestParam(value = "start") LocalDateTime start,
                                           @DateTimeFormat(pattern = PATTERN_FOR_TIME) @RequestParam(value = "end") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique);

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsRequestDto postStats(@RequestBody @Valid StatsRequestDto statsRequestDto);

}
