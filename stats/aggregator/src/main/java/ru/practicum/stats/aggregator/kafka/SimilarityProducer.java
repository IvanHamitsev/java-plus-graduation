package ru.practicum.stats.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import ru.practicum.stats.avro.EventSimilarityAvro;

@RequiredArgsConstructor
@Slf4j
public class SimilarityProducer {
    final Producer<String, EventSimilarityAvro> producer;
    final String topic;

    public void sendMessage(EventSimilarityAvro eventAvro) {
        ProducerRecord<String, EventSimilarityAvro> producerRecord = new ProducerRecord<>(topic, eventAvro);
        producer.send(producerRecord);
        producer.flush();
        log.info("Send similarity of {} {} ", eventAvro.getEventA(), eventAvro.getEventB());
    }

    public void flush() {
        producer.flush();
    }

    public void close() {
        producer.close();
    }
}
