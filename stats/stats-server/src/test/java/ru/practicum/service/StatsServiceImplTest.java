package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(StatsServiceImpl.class)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:test-stats-database",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.sql.init.schema-locations=classpath:test-schema.sql"}) // тестовая схема может отличаться,
// если штатная БД будет не h2
@DataJpaTest
class StatsServiceImplTest {
    @Autowired
    private final StatsServiceImpl statsService;

    @Test
    void save() {
        LocalDateTime nowTime = LocalDateTime.now();
        StatsRequestDto requestDto = new StatsRequestDto("application-name", "any-uri", "192.168.0.10", nowTime);
        StatsRequestDto requestDtoWithNullApplication = new StatsRequestDto(null, "any-uri", "192.168.0.10", nowTime);
        StatsRequestDto requestDtoWithNullUri = new StatsRequestDto("application-name", null, "192.168.0.10", nowTime);
        StatsRequestDto requestDtoWithNullIp = new StatsRequestDto("application-name", "any-uri", null, nowTime);

        assertDoesNotThrow(() -> statsService.save(requestDto));
        assertThrows(ValidationException.class, () -> statsService.save(requestDtoWithNullApplication));
        assertThrows(ValidationException.class, () -> statsService.save(requestDtoWithNullUri));
        assertThrows(ValidationException.class, () -> statsService.save(requestDtoWithNullIp));

        var resultRequestDto = statsService.save(requestDto);

        assertNotNull(resultRequestDto);
        assertEquals(resultRequestDto.getIp(), requestDto.getIp());
        assertEquals(resultRequestDto.getApp(), requestDto.getApp());
        assertEquals(resultRequestDto.getUri(), requestDto.getUri());
        assertEquals(resultRequestDto.getTimestamp().toString(), requestDto.getTimestamp().toString());
    }

    @Test
    void getStats() {
        LocalDateTime nowTime = LocalDateTime.now();
        StatsRequestDto request1Dto = new StatsRequestDto("application-name", "any-uri", "192.168.0.10", nowTime);
        StatsRequestDto request2Dto = new StatsRequestDto("application-name", "any-uri", "192.168.0.10", nowTime);
        StatsRequestDto request3Dto = new StatsRequestDto("application-name", "any-uri2", "192.168.0.10", nowTime);
        StatsRequestDto request4Dto = new StatsRequestDto("application-name", "any-uri", "192.168.0.11", nowTime);
        StatsRequestDto request5Dto = new StatsRequestDto("application2-name", "any-uri", "192.168.0.10", nowTime);

        statsService.save(request1Dto);
        statsService.save(request2Dto);
        statsService.save(request3Dto);
        statsService.save(request4Dto);
        statsService.save(request5Dto);

        assertThrows(ValidationException.class, () -> statsService.getStats(nowTime, nowTime.minusMinutes(1), null, true));
        assertDoesNotThrow(() -> statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), null, true));
        assertDoesNotThrow(() -> statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of(), true));
        assertDoesNotThrow(() -> statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of("any-uri"), true));
        assertDoesNotThrow(() -> statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of("any-uri"), false));

        var result1 = statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of("any-uri"), true);
        var result2 = statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of("any-uri"), false);
        var result3 = statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), null, true);
        var result4 = statsService.getStats(nowTime.minusMinutes(1), nowTime.plusMinutes(1), List.of(), false);

        assertEquals(result1.getFirst().getHits(), 2);
        assertEquals(result2.getFirst().getHits(), 3);
        assertEquals(result3.getFirst().getHits(), 2);
        assertEquals(result4.getFirst().getHits(), 3);
    }
}