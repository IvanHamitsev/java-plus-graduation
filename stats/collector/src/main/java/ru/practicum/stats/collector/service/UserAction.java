package ru.practicum.stats.collector.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.stats.avro.ActionTypeAvro;
import ru.practicum.stats.avro.UserActionAvro;
import ru.practicum.stats.collector.kafka.ActionProducer;
import ru.practicum.stats.proto.UserActionProto;

import java.time.Instant;

@Component
@AllArgsConstructor
public class UserAction {
    private final ActionProducer producer;

    public void process(UserActionProto proto) {
        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(
                        ActionTypeAvro.valueOf(proto.getActionType().name().replace("ACTION_", "")))
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()))
                .build();
        producer.send(actionAvro);
    }

}
