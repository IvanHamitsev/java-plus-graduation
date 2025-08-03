package ru.practicum.api.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

public interface UserInterface {
    @GetMapping("/admin/users")
    List<UserDto> getUsersList(@RequestParam(required = false) List<Long> ids,
                               @RequestParam(required = false, defaultValue = "0") Integer from,
                               @RequestParam(required = false, defaultValue = "10") Integer size);

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserDto addUser(@Valid @RequestBody UserDto newUser) throws ConflictException;

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteUser(@PathVariable Long userId);

    @GetMapping("/inner/user/{userId}")
    UserDto getById(@PathVariable Long userId) throws NotFoundException;

    @GetMapping("/inner/user/{userId}/exist")
    boolean existsById(@PathVariable Long userId);
}
