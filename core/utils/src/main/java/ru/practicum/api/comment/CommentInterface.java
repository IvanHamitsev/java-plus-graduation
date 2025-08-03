package ru.practicum.api.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Collection;

public interface CommentInterface {
    @GetMapping("/events/{eventId}/comments")
    Collection<CommentDto> getByEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) throws NotFoundException;

    @GetMapping("/admin/comments")
    Collection<CommentDto> getComments(
            @RequestParam("eventId") @Positive Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size
    ) throws NotFoundException;

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeComment(@PathVariable("commentId") Long commentId) throws NotFoundException;

    @PostMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    CommentDto addComments(
            @PathVariable Long userId,
            @RequestParam @Positive Long eventId,
            @RequestBody @Validated CommentDto commentDto
    ) throws ConflictException, NotFoundException;

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteComment(
            @PathVariable @NonNull Long commentId,
            @PathVariable @NonNull Long userId
    ) throws ConflictException, NotFoundException;

    @PatchMapping("/users/{userId}/comments/{commentId}")
    CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentDto commentDto
    ) throws ConflictException, NotFoundException;

    @GetMapping("/users/{userId}/comments")
    Collection<CommentDto> getByUserComment(@PathVariable Long userId) throws NotFoundException;
}
