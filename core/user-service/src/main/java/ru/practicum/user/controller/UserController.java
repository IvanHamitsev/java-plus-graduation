package ru.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.user.UserInterface;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController implements UserInterface {

    private final UserService userService;

    @Override
    public List<UserDto> getUsersList(@RequestParam(required = false) List<Long> ids,
                                      @RequestParam(required = false, defaultValue = "0") Integer from,
                                      @RequestParam(required = false, defaultValue = "10") Integer size) {
        return userService.getUsersByIdList(ids, PageRequest.of(from, size));
    }

    @Override
    public UserDto addUser(@Valid @RequestBody UserDto newUser) throws ConflictException {
        return userService.addUser(newUser);
    }

    @Override
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @Override
    public UserDto getById(@PathVariable Long userId) throws NotFoundException {
        return userService.getUserById(userId);
    }

    @Override
    public boolean existsById(@PathVariable Long userId) {
        try {
            userService.getUserById(userId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

}
