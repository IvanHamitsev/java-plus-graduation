package ru.practicum.stats.analyzer.mapper;

import ru.practicum.stats.proto.RecommendedEventProto;

public class RecommendationsMapper {
    public static RecommendedEventProto mapSimilarityToRecommendation(Long eventId, Double score) {
        return RecommendedEventProto.newBuilder()
                .setEventId(eventId)
                .setScore(score)
                .build();
    }
}
