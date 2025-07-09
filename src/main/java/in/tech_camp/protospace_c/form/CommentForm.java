package in.tech_camp.protospace_c.form;

import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentForm {
  @NotBlank(message = "コメントを空欄にすることはできません", groups = ValidationPriority1.class)
  private String text;

  private MultipartFile image;

  private Integer parentId;
}