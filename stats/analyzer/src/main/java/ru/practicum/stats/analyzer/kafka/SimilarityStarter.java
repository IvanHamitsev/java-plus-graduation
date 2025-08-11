package ru.practicum.stats.analyzer.kafka;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.stats.analyzer.service.RecommendationsService;
import ru.practicum.stats.avro.EventSimilarityAvro;

import java.time.Duration;

@Component
@Slf4j
public class SimilarityStarter implements Runnable {
    private final Consumer<String, EventSimilarityAvro> consumer;
    private final RecommendationsService service;

    public SimilarityStarter(Consumer<String, EventSimilarityAvro> consumer, RecommendationsService service) {
        this.consumer = consumer;
        this.service = service;
    }

    @Override
    public void run() {
        try {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    service.saveEventSimilarity(record.value());
                }
            }
        } catch (WakeupException e) {
            // nothing
        } catch (Exception e) {
            log.error("Error in similarity consumer {}: {} ", e, e.getMessage());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
                log.info("Similarity consumer closed");
            }
        }
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setName("similarity");
        thread.start();
    }
}
