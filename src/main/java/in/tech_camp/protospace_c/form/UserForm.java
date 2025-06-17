package in.tech_camp.protospace_c.form;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.BindingResult;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import in.tech_camp.protospace_c.validation.ValidationPriority2;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 用户注册表格，表单类和实体类都要打 Data 标签的原因个人认为也比较简单，因为这两种数据都是会直接给到前端去
// 并使用其属性生成前端视图，或是接受前端视图获得的数据的，本质上承担着数据流的任务
@Data
public class UserForm {
  // 高优先级非空验证
  @NotBlank(message = "Nickname can't be blank", groups = ValidationPriority1.class)
  private String nickname;

  // 邮箱不能为空
  @NotBlank(message = "Email can't be blank", groups = ValidationPriority1.class)
  // 非空后验证邮箱格式正确性
  @Email(message = "Email should be valid", groups = ValidationPriority2.class)
  private String email;

  // 高优先级非空验证
  @NotBlank(message = "Profile can't be blank", groups = ValidationPriority1.class)
  private String profile;

  // 高优先级非空验证
  @NotBlank(message = "Affiliation can't be blank", groups = ValidationPriority1.class)
  private String affiliation;

  // 高优先级非空验证
  @NotBlank(message = "Position can't be blank", groups = ValidationPriority1.class)
  private String position;

  @NotBlank(message = "Password can't be blank", groups = ValidationPriority1.class)
  @Length(min = 6, max = 128, message = "Password should be between 6 and 128 characters", groups = ValidationPriority2.class)
  private String password;

  private String passwordConfirmation;

  // 验证确认密码栏的值是不是和已经输入的密码一样
  // 这里边 Validation 包没有直接功能一致的标签，所以要自己写一个函数
  // 传入一个 BindingResult 对象是为了能够将验证失败的结果绑定在该对象上方便进行读取与可视化
  public void validatePasswordConfirmation(BindingResult result) {
      if (!password.equals(passwordConfirmation)) {
          result.rejectValue("passwordConfirmation", "error.user", "Password confirmation doesn't match Password");
      }
  }
}
