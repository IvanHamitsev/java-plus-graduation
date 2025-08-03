package ru.practicum.api.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.user.UserInterface;

@FeignClient(name = "user-service")
public interface UserClient extends UserInterface {
}
