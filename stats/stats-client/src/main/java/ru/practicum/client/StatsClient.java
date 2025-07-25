package ru.practicum.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsClient {
    final RestClient client;

    public StatsClient(@Value("${stats-server.url}") String connectionURL) {
        this.client = RestClient.create(connectionURL);
    }

    public Collection<StatsResponseDto> getAllStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", String.join(",", uris))
                .queryParam("unique", Optional.ofNullable(unique).orElse(false))
                .build()
                .toUriString();

        StatsResponseDto[] response = client.get()
                .uri(uri)
                .retrieve()
                .body(StatsResponseDto[].class);

        // Возвращаем пустую коллекцию, если результат null
        return response != null ? Arrays.asList(response) : List.of();
    }

    public StatsResponseDto postStats(StatsRequestDto statsRequestDto) {
        return Optional.ofNullable(client.post()
                        .uri("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(statsRequestDto)
                        .retrieve()
                        .body(StatsResponseDto.class))
                .orElseThrow(() -> new IllegalStateException("Failed to post stats: response is null"));
    }
}
