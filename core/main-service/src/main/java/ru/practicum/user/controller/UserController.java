package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/admin/users")
public class UserController {

    final UserService userService;

    @GetMapping
    public List<UserDto> getUsersList(@RequestParam(required = false) List<Long> ids,
                                      @RequestParam(required = false, defaultValue = "0") Integer from,
                                      @RequestParam(required = false, defaultValue = "10") Integer size) {
        return userService.getUsersByIdList(ids, PageRequest.of(from, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody UserDto newUser) throws ConflictException {
        return userService.addUser(newUser);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

}
