package ru.practicum.stats.aggregator.kafka;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.stats.aggregator.service.UserAction;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Duration;

@AllArgsConstructor
@Component
@Slf4j
public class AggregatorStarter implements Runnable {
    private final Consumer<String, UserActionAvro> userActionConsumer;
    private final UserAction userActionProducer;

    @Override
    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = userActionConsumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    userActionProducer.process(record.value());
                    userActionProducer.flush();
                    userActionConsumer.commitAsync();
                }
            }
        } catch (WakeupException e) {
            // nothing to do
        } catch (Exception e) {
            log.error("Error {}: {}", e, e.getMessage());
        } finally {
            try {
                userActionProducer.flush();
                userActionConsumer.commitSync();
            } finally {
                userActionConsumer.close();
                userActionProducer.close();
                log.info("Consumer and producer closed");
            }
        }
    }

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.setName("aggregator");
        thread.start();
    }
}
