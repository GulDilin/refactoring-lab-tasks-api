package guldilin.dto;

import lombok.*;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidationErrorDTO {
    Map<String, String> message;
    String error;
}
