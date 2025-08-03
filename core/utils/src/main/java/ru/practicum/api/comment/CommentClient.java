package ru.practicum.api.comment;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.comment.CommentInterface;

@FeignClient(name = "comment-service")
public interface CommentClient extends CommentInterface {
}
