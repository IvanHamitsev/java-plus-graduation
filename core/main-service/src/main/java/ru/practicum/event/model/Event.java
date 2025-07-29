package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 1024)
    String annotation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    Category category;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(length = 1024)
    String description;

    @Column(nullable = false, name = "event_date")
    LocalDateTime eventDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    User initiator;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    Location location;

    @Column
    Boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    EventState state;

    @Column(nullable = false)
    String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "events_compilations",
            joinColumns = @JoinColumn(name = "compilation"),
            inverseJoinColumns = @JoinColumn(name = "event"))
    List<Compilation> compilationList;
}
