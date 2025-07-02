package in.tech_camp.protospace_c.form;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import in.tech_camp.protospace_c.validation.ValidationPriority2;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageForm {
  @NotBlank(message = "メッセージを入力してください", groups = ValidationPriority1.class)
  @Size(max = 200, message = "メッセージは200文字以内で入力してください", groups = ValidationPriority2.class)
  private String text;
}
