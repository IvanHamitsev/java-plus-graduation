package ru.practicum.stats.analyzer.service;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.analyzer.mapper.RecommendationsMapper;
import ru.practicum.stats.analyzer.mapper.SimilarityMapper;
import ru.practicum.stats.analyzer.model.EventSimilarity;
import ru.practicum.stats.analyzer.model.UserAction;
import ru.practicum.stats.analyzer.repository.SimilarityRepository;
import ru.practicum.stats.analyzer.repository.UserActionRepository;
import ru.practicum.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RecommendationsService {
    private final SimilarityRepository similarityRepository;
    private final UserActionRepository actionRepository;

    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        List<Long> userRecentActionsEvents = actionRepository.findRecentEventIdsByUserId(
                request.getUserId(), request.getMaxResult()
        );
        List<UserAction> allUserInteraction = actionRepository.findAllInteractionsByUser(request.getUserId());
        if (userRecentActionsEvents.isEmpty()) {
            return;
        }

        // Идентификаторы похожих событий
        List<Long> similarities = similarityRepository.findSimilarUnseenEvents(
                        userRecentActionsEvents,
                        allUserInteraction.stream().map(UserAction::getEventId).toList(),
                        request.getMaxResult()
                ).stream().map(es -> userRecentActionsEvents.contains(es.getFirst()) ? es.getSecond() : es.getFirst())
                .toList();

        // Событие и оценка
        Map<Long, Double> eventScore = allUserInteraction.stream()
                .collect(Collectors.toMap(UserAction::getEventId, UserAction::getScore));

        List<RecommendedEventProto> result = new ArrayList<>();
        // Максимально похожие события (с которыми уже взаимодействовали и нет)
        List<EventSimilarity> allSimilaritiesForSimilarities = similarityRepository
                .findTopKSimilarUserEvents(similarities,
                        allUserInteraction.stream().map(UserAction::getEventId).toList(), request.getMaxResult());

        for (Long eventId : similarities) {
            // Определяем топ-сходства для события
            List<EventSimilarity> top = allSimilaritiesForSimilarities.stream()
                    .filter(e -> Objects.equals(e.getFirst(), eventId) || Objects.equals(e.getSecond(), eventId))
                    .toList();

            double weightedSum = 0;
            double simSum = 0; // коэффициенты подобия событий
            for (EventSimilarity es : top) {
                Long neighborId = es.getFirst().equals(eventId) ? es.getSecond() : es.getFirst();
                Double neighborScore = eventScore.get(neighborId);
                if (neighborScore != null) {
                    weightedSum += neighborScore * es.getScore(); // оценка умножается на коэффициент подобия
                    simSum += es.getScore();
                }
            }
            // Итого предсказанная оценка
            double predictedScore = simSum > 0 ? weightedSum / simSum : 0;

            result.add(RecommendedEventProto.newBuilder().setEventId(eventId).setScore(predictedScore).build());
        }
        result = result.stream()
                .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResult())
                .toList();

        for (RecommendedEventProto proto : result) {
            responseObserver.onNext(proto);
        }
    }

    public void saveEventSimilarity(EventSimilarityAvro avro) {
        similarityRepository.deleteById(SimilarityMapper.mapAvroToKey(avro));
        similarityRepository.save(SimilarityMapper.mapAvroToEntity(avro));
    }

    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> eventsSimilarity = similarityRepository.findByEventIdForUser(request.getEventId(),
                        request.getUserId(), request.getMaxResult()).stream()
                .map(e -> RecommendationsMapper.mapSimilarityToRecommendation(
                        e.getFirst() == request.getEventId() ? e.getSecond() : e.getFirst(), e.getScore()
                )).toList();
        for (RecommendedEventProto proto : eventsSimilarity) {
            responseObserver.onNext(proto);
        }
    }

    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        List<RecommendedEventProto> eventInteractions = actionRepository.findInteractions(request.getEventIdList())
                .stream().map(
                        e -> RecommendedEventProto.newBuilder().setEventId((long) e[0]).setScore((double) e[1]).build()
                ).toList();
        for (RecommendedEventProto proto : eventInteractions) {
            responseObserver.onNext(proto);
        }
    }
}
