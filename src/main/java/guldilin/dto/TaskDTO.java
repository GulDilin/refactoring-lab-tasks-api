package guldilin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import guldilin.errors.ErrorMessage;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class TaskDTO {
    @NotBlank(message = ErrorMessage.NOT_BLANK)
    private String title;

    @Builder.Default
    private String description = "";

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @NotNull(message = ErrorMessage.NOT_NULL)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date deadline;
}
