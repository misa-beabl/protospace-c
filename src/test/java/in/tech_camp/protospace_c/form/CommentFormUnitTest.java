package in.tech_camp.protospace_c.form;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import in.tech_camp.protospace_c.form.CommentForm;
import in.tech_camp.protospace_c.validation.ValidationPriority1;
import in.tech_camp.protospace_c.factory.CommentFormFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;


@ActiveProfiles("test")
public class CommentFormUnitTest {
    private Validator validator;
    private CommentForm commentForm;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        commentForm = CommentFormFactory.createCommentForm();
    }

    @Nested
    class コメント作成ができる場合 {
        @Test
        public void テキストが存在していれば投稿できる () {
          Set<ConstraintViolation<CommentForm>> violations = validator.validate(commentForm, ValidationPriority1.class);
          assertEquals(0, violations.size());
        }
    }

    @Nested
    class コメント作成ができない場合 {
        @Test
        public void テキストが空ではバリデーションエラーが発生する() {
          commentForm.setText("");
          Set<ConstraintViolation<CommentForm>> violations = validator.validate(commentForm, ValidationPriority1.class);
          assertEquals(1, violations.size());
          assertEquals("Comment can't be blank", violations.iterator().next().getMessage());
        }
    }
}