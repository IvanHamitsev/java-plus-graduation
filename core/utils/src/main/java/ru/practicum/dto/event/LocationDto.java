package ru.practicum.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    Long id;

    Float lat;

    Float lon;
}
