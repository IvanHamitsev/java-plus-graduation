package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "stats-server", configuration = StatsFeignClientConfig.class, fallbackFactory = StatsClientFallbackFactory.class)
public interface StatsClient extends StatsClientInterface {
}