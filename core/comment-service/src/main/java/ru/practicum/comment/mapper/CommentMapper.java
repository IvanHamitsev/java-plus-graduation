package ru.practicum.comment.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.user.UserDto;

import java.util.List;

@UtilityClass
public class CommentMapper {

    public CommentDto mapToCommentDto(final Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .userId(comment.getUser())
                .eventId(comment.getEvent())
                .content(comment.getContent())
                .created(comment.getCreated())
                .isInitiator(comment.isInitiator())
                .build();
    }

    public List<CommentDto> mapToCommentDto(final List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }
        return comments.stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    public Comment mapTo(final CommentDto comment, final UserDto user, final EventFullDto event) {
        return Comment.builder()
                .id(comment.getId())
                .user(user.getId())
                .event(event.getId())
                .content(comment.getContent())
                .build();
    }
}
