package in.tech_camp.protospace_c.form;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentForm {
  @NotBlank(message = "Comment can't be blank", groups = ValidationPriority1.class)
  private String text;
}