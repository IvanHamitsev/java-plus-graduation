package ru.practicum.stats.analyzer.kafka;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.stats.analyzer.service.UserActionService;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Duration;

@Component
@Slf4j
public class UserActionStarter implements Runnable {
    private final Consumer<String, UserActionAvro> consumer;
    private final UserActionService service;

    public UserActionStarter(Consumer<String, UserActionAvro> consumer, UserActionService service) {
        this.consumer = consumer;
        this.service = service;
    }

    @Override
    public void run() {
        try {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    service.saveUserAction(record.value());
                }
            }
        } catch (WakeupException e) {
            // nothing
        } catch (Exception e) {
            log.error("Error in user action consumer {}: {} ", e, e.getMessage());
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
                log.info("User action consumer closed");
            }
        }
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setName("action");
        thread.start();
    }
}
