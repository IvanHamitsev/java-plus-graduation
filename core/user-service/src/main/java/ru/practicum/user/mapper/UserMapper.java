package ru.practicum.user.mapper;

import ru.practicum.dto.user.UserDto;
import ru.practicum.user.model.User;

public class UserMapper {
    public static User mapUserDto(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public static UserDto mapUser(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}
