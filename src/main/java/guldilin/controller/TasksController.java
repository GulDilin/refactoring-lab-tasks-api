package guldilin.controller;

import guldilin.dto.TaskDTO;
import guldilin.dto.TasksListDTO;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import javax.validation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TasksController {
    private final List<TaskDTO> tasksList;

    public TasksController() {
        this.tasksList = new LinkedList<>();
    }

    @SneakyThrows
    @GetMapping
    public TasksListDTO getItems(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String[] tags
    ) {
        List<TaskDTO> tasks = tasksList;
        if (tags != null) {
            List<String> tagsList = Arrays.stream(tags).map(String::trim).collect(Collectors.toList());
            tasks = tasksList
                    .stream()
                    .filter(task -> tagsList.stream()
                            .anyMatch(tag -> task.getTags()
                                    .stream()
                                    .anyMatch(tag::equals)))
                    .collect(Collectors.toList());
        }
        offset = Optional.ofNullable(offset).orElse(0);
        limit = Optional.ofNullable(limit).orElse(tasksList.size());
        Comparator<TaskDTO> byDeadline = Comparator.comparing(TaskDTO::getDeadline);
        return TasksListDTO
                .builder()
                .result(tasks
                        .stream()
                        .sorted(byDeadline)
                        .skip(offset)
                        .limit(limit)
                        .collect(Collectors.toList()))
                .total((long) tasks.size())
                .build();
    }

    @SneakyThrows
    @PostMapping
    public TaskDTO createTask(@RequestBody TaskDTO task) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<TaskDTO>> constraintViolations = validator.validate(task);
        if (constraintViolations.size() > 0) throw new ConstraintViolationException(constraintViolations);

        task = TaskDTO.builder()
                .title(task.getTitle())
                .deadline(task.getDeadline())
                .tags(Optional.ofNullable(task.getTags()).orElse(new ArrayList<>()))
                .description(Optional.ofNullable(task.getDescription()).orElse(""))
                .build();
        tasksList.add(task);
        return task;
    }
}
