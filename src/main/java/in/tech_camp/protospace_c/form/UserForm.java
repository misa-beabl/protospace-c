package in.tech_camp.protospace_c.form;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import in.tech_camp.protospace_c.validation.ValidationPriority2;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserForm {
  @NotBlank(message = "Nickname can't be blank", groups = ValidationPriority1.class)
  private String nickname;

  @NotBlank(message = "Email can't be blank", groups = ValidationPriority1.class)
  @Email(message = "Email should be valid", groups = ValidationPriority2.class)
  private String email;

  @NotBlank(message = "Profile can't be blank", groups = ValidationPriority1.class)
  private String profile;

  @NotBlank(message = "Affiliation can't be blank", groups = ValidationPriority1.class)
  private String affiliation;

  @NotBlank(message = "Position can't be blank", groups = ValidationPriority1.class)
  private String position;

  @NotBlank(message = "Password can't be blank", groups = ValidationPriority1.class)
  @Length(min = 6, max = 128, message = "Password should be between 6 and 128 characters", groups = ValidationPriority2.class)
  private String password;

  private String passwordConfirmation;

  public void validatePasswordConfirmation(BindingResult result) {
      if (!password.equals(passwordConfirmation)) {
          result.rejectValue("passwordConfirmation", "error.user", "Password confirmation doesn't match Password");
      }
  }

  private MultipartFile avatar;
}
