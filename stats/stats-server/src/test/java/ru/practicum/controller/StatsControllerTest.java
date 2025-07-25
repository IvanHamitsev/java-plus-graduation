package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.StatsServer;
import ru.practicum.dto.StatsRequestDto;
import ru.practicum.dto.StatsResponseDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StatsServer.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    private StatsRequestDto statsRequestDto;
    private StatsResponseDto statsResponseDto;

    @BeforeEach
    void setUp() {
        statsRequestDto = new StatsRequestDto();
        statsRequestDto.setUri("/test");
        statsRequestDto.setApp("test-app");
        statsRequestDto.setIp("192.168.1.1");
        statsRequestDto.setTimestamp(LocalDateTime.now());

        statsResponseDto = new StatsResponseDto();
        statsResponseDto.setApp("test-app");
        statsResponseDto.setUri("/test");
        statsResponseDto.setHits(1L);
    }

    @Test
    void getStats_shouldReturnStats() throws Exception {
        Mockito.when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), anyBoolean()))
                .thenReturn(Collections.singletonList(statsResponseDto));

        mockMvc.perform(get("/stats")
                        .param("start", "2024-12-01 00:00:00")
                        .param("end", "2024-12-15 23:59:59")
                        .param("uris", "/test")
                        .param("unique", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("test-app"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(1));
    }

    @Test
    void save_shouldReturnSavedHit() throws Exception {
        Mockito.when(statsService.save(any(StatsRequestDto.class))).thenReturn(statsRequestDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"app\":\"test-app\",\"uri\":\"/test\",\"ip\":\"192.168.1.1\",\"timestamp\":\"2024-12-15 12:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("test-app"))
                .andExpect(jsonPath("$.uri").value("/test"))
                .andExpect(jsonPath("$.ip").value("192.168.1.1"));
    }
}
