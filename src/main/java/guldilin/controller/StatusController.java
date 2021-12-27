package guldilin.controller;

import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/status")
@RestController
public class StatusController {
    @SneakyThrows
    @GetMapping
    public String getStatus() {
        return "OK";
    }
}
