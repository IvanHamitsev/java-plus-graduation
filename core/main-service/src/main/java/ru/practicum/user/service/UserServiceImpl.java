package ru.practicum.user.service;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto addUser(UserDto newUserDto) throws ConflictException {
        if (userRepository.existsByName(newUserDto.getName())) {
            throw new ConflictException(String.format("Пользователь %s уже существует", newUserDto.getName()));
        }
        User savedUser = userRepository.save(UserMapper.mapUserDto(newUserDto));
        return UserMapper.mapUser(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) throws NotFoundException {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("Пользователь с id = %d не найден", userId)));
        return UserMapper.mapUser(user);
    }

    @Override
    public List<UserDto> getUsersByIdList(List<Long> ids, Pageable page) {
        List<User> users = (ids == null || ids.isEmpty()) ?
                userRepository.findAll(page).getContent() :
                userRepository.findAllByIdsPageable(ids, page);
        return users.stream()
                .map(UserMapper::mapUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
