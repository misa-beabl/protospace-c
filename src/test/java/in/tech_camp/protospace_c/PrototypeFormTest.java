package in.tech_camp.protospace_c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import in.tech_camp.protospace_c.form.PrototypeForm;
import in.tech_camp.protospace_c.validation.ValidationPriority1;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ActiveProfiles("test")
public class PrototypeFormTest {
  private PrototypeForm prototypeForm;
  private Validator validator;

  @BeforeEach
    public void setUp() {
      ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      validator = factory.getValidator();
      prototypeForm = PrototypeFactory.createPrototypeForm();
    }

    @Test
  public void 正常な値の場合はエラーが発生しない() {
    Set<ConstraintViolation<PrototypeForm>> violations = validator.validate(prototypeForm, ValidationPriority1.class);
    assertEquals(0, violations.size());
  }


  @Test
  public void nameが空の場合エラーが発生する() {
    prototypeForm.setName(null);

    Set<ConstraintViolation<PrototypeForm>> violations = validator.validate(prototypeForm, ValidationPriority1.class);
    assertEquals(1, violations.size());
    assertEquals("Name can't be blank", violations.iterator().next().getMessage());
  }

  @Test
  public void sloganが空の場合エラーが発生する() {
    prototypeForm.setSlogan(null);

    Set<ConstraintViolation<PrototypeForm>> violations = validator.validate(prototypeForm, ValidationPriority1.class);
    assertEquals(1, violations.size());
    assertEquals("Slogan can't be blank", violations.iterator().next().getMessage());
  }

  @Test
  public void conceptが空の場合エラーが発生する() {
    prototypeForm.setConcept(null);

    Set<ConstraintViolation<PrototypeForm>> violations = validator.validate(prototypeForm, ValidationPriority1.class);
    assertEquals(1, violations.size());
    assertEquals("Concept can't be blank", violations.iterator().next().getMessage());
  }

  @Test
  public void imageが空の場合エラーが発生する() {
    prototypeForm.setImage(null);

    Set<ConstraintViolation<PrototypeForm>> violations = validator.validate(prototypeForm, ValidationPriority1.class);
    assertEquals(1, violations.size());
    assertEquals("Image can't be blank", violations.iterator().next().getMessage());
  }
}
