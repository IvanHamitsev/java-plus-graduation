package ru.practicum.api.compilation;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.compilation.AdminCompilationInterface;
import ru.practicum.api.compilation.PublicCompilationInterface;

@FeignClient(name = "compilation-service")
public interface CompilationClient extends AdminCompilationInterface, PublicCompilationInterface {
}
