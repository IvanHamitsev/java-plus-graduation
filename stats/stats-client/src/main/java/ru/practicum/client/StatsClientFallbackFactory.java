package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class StatsClientFallbackFactory implements FallbackFactory<StatsClient> {
    @Override
    public StatsClient create(Throwable cause) {
        return new StatsClient() {
            @Override
            public List<StatsResponseDto> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
                log.warn("Service stats-server not responds. Method getAllStats fails with cause {}", cause.getMessage());
                return List.of();
            }

            @Override
            public StatsRequestDto postStats(StatsRequestDto statsRequestDto) {
                log.warn("Service stats-server not responds. Method postStats fails with cause {}", cause.getMessage());
                return null;
            }
        };
    }
}
