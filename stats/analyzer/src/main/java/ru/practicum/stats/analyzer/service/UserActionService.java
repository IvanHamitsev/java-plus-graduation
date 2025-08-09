package ru.practicum.stats.analyzer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.analyzer.mapper.UserActionMapper;
import ru.practicum.stats.analyzer.model.UserActionId;
import ru.practicum.stats.analyzer.repository.UserActionRepository;
import ru.practicum.stats.avro.UserActionAvro;

@Slf4j
@Service
@AllArgsConstructor
public class UserActionService {
    private final UserActionRepository repository;

    public void saveUserAction(UserActionAvro actionAvro) {
        log.trace("User {} perform action {} for {}", actionAvro.getUserId(), actionAvro.getActionType(), actionAvro.getEventId());
        UserActionId id = UserActionMapper.mapAvroToKey(actionAvro);
        if (!repository.existsById(id) ||
                repository.findById(id).get().getScore() < UserActionMapper.convertActionToWeight(actionAvro.getActionType())) {
            repository.save(UserActionMapper.mapAvroToEntity(actionAvro));
        }
    }
}
