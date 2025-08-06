package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.api.comment.GetCommentsAdminRequest;
import ru.practicum.api.event.EventClient;
import ru.practicum.api.user.UserClient;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventState;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CommentDto addComment(final CommentDto commentDto, Long userId, Long eventId) throws NotFoundException, ConflictException {
        commentDto.setUserId(userId);
        commentDto.setEventId(eventId);

        UserDto user = fetchUser(userId);
        EventFullDto event = fetchEvent(eventId);
        if (!EventState.PUBLISHED.equals(event.getState())) {
            log.warn("Невозможно добавить комментарий к событию, которое не опубликовано, состояние события = {}",
                    event.getState());
            throw new ConflictException("Невозможно сохранить комментарии для неопубликованного события.");
        }
        Comment comment = CommentMapper.mapTo(commentDto, user, event);
        comment.setCreated(LocalDateTime.now());
        if (user.getId().equals(event.getInitiator().getId())) {
            comment.setInitiator(true);
        }
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(savedComment);

    }

    @Override
    @Transactional
    public void delete(final Long userId, final Long commentId) throws NotFoundException, ConflictException {
        Comment comment = fetchComment(commentId);

        if (!comment.getUser().equals(userId)) {
            throw new ConflictException("Пользователь может удалять только свои комментарии.");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void delete(final Long commentId) throws NotFoundException {
        Comment comment = fetchComment(commentId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto updateUserComment(final Long userId, final Long commentId,
                                        final CommentDto commentDto) throws NotFoundException, ConflictException {
        Comment comment = fetchComment(commentId);
        fetchUser(userId);

        if (!comment.getUser().equals(userId)) {
            throw new ConflictException("Пользователь может удалять только свои комментарии.");
        }

        comment.setContent(commentDto.getContent());
        Comment updated = commentRepository.save(comment);

        return CommentMapper.mapToCommentDto(updated);
    }

    @Override
    public List<CommentDto> getAllUserComments(final Long userId) throws NotFoundException {
        UserDto user = fetchUser(userId);
        return CommentMapper.mapToCommentDto(commentRepository.findByUser(user.getId()));
    }

    @Override
    public List<CommentDto> getAllEventComments(final GetCommentsAdminRequest param) throws NotFoundException {
        final List<Comment> comments =
                getEventComments(param.getEventId(), param.getFrom(), param.getSize());
        return CommentMapper.mapToCommentDto(comments);
    }

    @Override
    public List<CommentDto> getAllEventComments(final Long eventId, final int from, final int size) throws NotFoundException {
        return CommentMapper.mapToCommentDto(getEventComments(eventId, from, size));
    }

    private List<Comment> getEventComments(final Long eventId, final int from, final int size) throws NotFoundException {
        if (!eventClient.existsById(eventId)) {
            log.warn("Событие с идентификатором {} не существует в базе данных.", eventId);
            throw new NotFoundException("Событие не найдено.");
        }
        final PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findAllByEvent(eventId, page).getContent();
    }

    private UserDto fetchUser(final Long userId) throws NotFoundException {
        return userClient.getById(userId);
    }

    private EventFullDto fetchEvent(final Long eventId) throws NotFoundException {
        return eventClient.getInnerEventById(eventId);
    }

    private Comment fetchComment(final Long commentId) throws NotFoundException {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий с идентификатором {} не найден.", commentId);
                    return new NotFoundException("Комментарий не найден.");
                });
    }
}
