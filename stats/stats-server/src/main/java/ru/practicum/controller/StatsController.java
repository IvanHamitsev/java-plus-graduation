package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.JsonFormatPattern.JSON_FORMAT_PATTERN_FOR_TIME;

@RestController
@Slf4j
public class StatsController {

    final StatsService statsService;

    @Autowired
    public StatsController(@Qualifier("statsServiceImpl") StatsService statsService) {
        this.statsService = statsService;
    }


    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<StatsResponseDto> getStats(@DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "start") LocalDateTime start,
                                           @DateTimeFormat(pattern = JSON_FORMAT_PATTERN_FOR_TIME) @RequestParam(value = "end") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("Получен запрос сбора статистики StartDate: {}, EndDate: {}, Uris: {}, Unique: {}", start, end, uris, unique);
        return statsService.getStats(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public StatsRequestDto save(@RequestBody @Valid StatsRequestDto statsRequestDto) {
        log.info("Получен запрос на добавление статистики: StatsRequestDto: {}", statsRequestDto);
        return statsService.save(statsRequestDto);
    }
}