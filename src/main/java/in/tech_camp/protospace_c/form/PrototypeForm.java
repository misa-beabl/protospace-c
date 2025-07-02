package in.tech_camp.protospace_c.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.validation.ValidationPriority1;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrototypeForm {
  @NotBlank(message = "Name can't be blank",groups = ValidationPriority1.class)
  private String name;

  @NotBlank(message = "Slogan can't be blank",groups = ValidationPriority1.class)
  private String slogan;

  @NotBlank(message = "Concept can't be blank",groups = ValidationPriority1.class)
  private String concept;

  @NotNull(message = "Genre must be selected", groups = ValidationPriority1.class)
  private Integer genreId;

  @NotNull(message = "Image can't be blank",groups = ValidationPriority1.class)
  private MultipartFile image;

  private List<Integer> selectedGenres;
}
