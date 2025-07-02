package in.tech_camp.protospace_c.form;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class UserIconForm {
  private MultipartFile avatar;
}
