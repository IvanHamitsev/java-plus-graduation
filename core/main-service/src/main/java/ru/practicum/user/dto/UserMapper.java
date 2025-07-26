package ru.practicum.user.dto;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.user.model.User;

@Component
@AllArgsConstructor
public class UserMapper {
    public static UserDto mapUser(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User mapUserDto(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
