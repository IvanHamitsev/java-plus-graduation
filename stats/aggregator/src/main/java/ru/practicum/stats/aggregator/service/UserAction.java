package ru.practicum.stats.aggregator.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.exception.IncorrectActionTypeException;
import ru.practicum.stats.aggregator.kafka.SimilarityProducer;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAction {
    final SimilarityProducer producer;

    // <событие, < пользователь, вес оценки пользователем события>>
    final Map<Long, Map<Long, Double>> usersFeedback;

    // <пара событий, сумма оценок общих пользователей>
    final Map<EventPair, Double> eventsMinWeightSum;

    // <событие, сумма всех оценок пользователей>
    final Map<Long, Double> eventWeightSum;

    // <пара событий, сходство двух событий>
    final Map<EventPair, Double> eventsSimilarity;

    // <событие, корень суммы всех оценок пользователей события>
    final Map<Long, Double> sqrtCache;


    @Autowired
    public UserAction(SimilarityProducer producer) {
        this.producer = producer;
        usersFeedback = new HashMap<>();
        eventsMinWeightSum = new HashMap<>();
        eventWeightSum = new HashMap<>();
        eventsSimilarity = new HashMap<>();
        sqrtCache = new HashMap<>();
    }

    public void process(UserActionAvro avro) throws IncorrectActionTypeException {
        Long userId = avro.getUserId();
        Long eventId = avro.getEventId();
        Double weight = convertActionToWeight(avro.getActionType());

        // есть ли оценки пользователей события?
        Map<Long, Double> userRatings = usersFeedback.computeIfAbsent(eventId, e -> new HashMap<>());
        Double oldWeight = userRatings.getOrDefault(userId, 0.0);

        if (oldWeight < weight) {
            userRatings.put(userId, weight);
            determineSimilarity(eventId, userId, oldWeight, weight, avro.getTimestamp());
        }
    }

    public void flush() {
        producer.flush();
    }

    public void close() {
        producer.close();
    }

    private void determineSimilarity(Long eventId, Long userId, Double oldWeight, Double newWeight, Instant timestamp) {

        // вычесть старую оценку пользователя и добавить новую
        double newSum = eventWeightSum.getOrDefault(eventId, 0.0) - oldWeight + newWeight;
        eventWeightSum.put(eventId, newSum);
        // старое значение убрать
        sqrtCache.remove(eventId);

        // пройти по каждому событию
        for (var eventEntry : usersFeedback.entrySet()) {
            Long otherEventId = eventEntry.getKey();
            var feedback = eventEntry.getValue();

            // пропускаем событие, с которым пользователь не взаимодействовал
            if (!feedback.containsKey(userId) || Objects.equals(otherEventId, eventId)) {
                continue;
            }
            double eventWeight = feedback.get(userId);
            EventPair eventPair = EventPair.of(eventId, otherEventId);
            // для данного пользователя сумма оценок обоих событий и сумма оценок самого события
            double oldMinSum = eventsMinWeightSum.getOrDefault(eventPair, 0.0);
            double newMinSum = oldMinSum - Math.min(oldWeight, eventWeight) + Math.min(newWeight, eventWeight);
            eventsMinWeightSum.put(eventPair, newMinSum);

            double similarity = calculateSimilarity(eventPair, newMinSum);

            eventsSimilarity.put(eventPair, similarity);

            EventSimilarityAvro message = EventSimilarityAvro.newBuilder()
                    .setEventA(eventPair.first())
                    .setEventB(eventPair.second())
                    .setTimestamp(timestamp)
                    .setScore(similarity)
                    .build();
            producer.sendMessage(message);
        }
    }

    private double calculateSimilarity(EventPair pair, double commonSum) {
        double sqrtA = getSqrtSum(pair.first());
        double sqrtB = getSqrtSum(pair.second());

        // оценка по формуле косинусного сходства
        double similarity = commonSum / (sqrtA * sqrtB);
        log.info("Calculated similarity {} for events {} and {}", similarity, pair.first(), pair.second());
        return similarity;
    }

    private double getSqrtSum(Long eventId) {
        return sqrtCache.computeIfAbsent(eventId, id -> Math.sqrt(eventWeightSum.getOrDefault(id, 0.0)));
    }

    private Double convertActionToWeight(ActionTypeAvro action) throws IncorrectActionTypeException {
        switch (action) {
            case VIEW -> {
                return 0.4;
            }
            case REGISTER -> {
                return 0.8;
            }
            case LIKE -> {
                return 1.0;
            }
            default -> {
                throw new IncorrectActionTypeException("Wrong type: " + action);
            }
        }
    }

    record EventPair(Long first, Long second) {
        public static EventPair of(Long a, Long b) {
            // события в паре упорядочены для избежания повтора пары
            return a < b ? new EventPair(a, b) : new EventPair(b, a);
        }
    }
}
