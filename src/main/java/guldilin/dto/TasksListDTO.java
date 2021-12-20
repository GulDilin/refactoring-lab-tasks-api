package guldilin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class TasksListDTO {
    private List<TaskDTO> result;
    private Long total;
}
