package ru.practicum.stats.collector.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.practicum.stats.avro.UserActionAvro;

@AllArgsConstructor
@Slf4j
public class ActionProducer {
    private Producer<String, UserActionAvro> producer;
    private final String topic;

    public void send(UserActionAvro action) {
        log.info("Send action {} user {} event {}", action.getActionType(), action.getUserId(), action.getEventId());
        ProducerRecord<String, UserActionAvro> record = new ProducerRecord<>(topic, action);
        producer.send(record);
        producer.flush();
        log.info("Send {} ok", action);
    }
}
