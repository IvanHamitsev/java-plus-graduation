package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.comment.CommentInterface;
import ru.practicum.api.comment.GetCommentsAdminRequest;
import ru.practicum.comment.service.CommentService;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@Validated
public class CommentController implements CommentInterface {

    private final CommentService service;

    @Override
    public Collection<CommentDto> getByEvent(Long eventId, Integer from, Integer size) throws NotFoundException {
        return service.getAllEventComments(eventId, from, size);
    }

    @Override
    public Collection<CommentDto> getComments(Long eventId, Integer from, Integer size) throws NotFoundException {
        return service.getAllEventComments(new GetCommentsAdminRequest(eventId, from, size));
    }

    @Override
    public void removeComment(@PathVariable("commentId") Long commentId) throws NotFoundException {
        service.delete(commentId);
    }

    @Override
    public CommentDto addComments(Long userId, Long eventId, CommentDto commentDto) throws ConflictException, NotFoundException {
        return service.addComment(commentDto, userId, eventId);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) throws ConflictException, NotFoundException {
        service.delete(userId, commentId);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, CommentDto commentDto) throws ConflictException, NotFoundException {
        return service.updateUserComment(userId, commentId, commentDto);
    }

    @Override
    public Collection<CommentDto> getByUserComment(Long userId) throws NotFoundException {
        return service.getAllUserComments(userId);
    }
}

