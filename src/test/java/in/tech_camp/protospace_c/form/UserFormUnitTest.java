package in.tech_camp.protospace_c.form;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;

import com.github.javafaker.Faker;

import in.tech_camp.protospace_c.factory.UserFormFactory;
import in.tech_camp.protospace_c.validation.ValidationPriority1;
import in.tech_camp.protospace_c.validation.ValidationPriority2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@ActiveProfiles("test")
public class UserFormUnitTest {

    private static final Faker faker = new Faker();
    private UserForm userForm;
    private Validator validator;
    private BindingResult bindingResult;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        userForm = UserFormFactory.createUser();
        bindingResult = Mockito.mock(BindingResult.class);
    }

    @Nested
    class ユーザー作成ができる場合 {
        @Test
        public void 有効なすべての値ならバリデーションエラーが発生しない() {
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(0, violations.size());
        }
    }

    @Nested
    class ユーザー作成ができない場合 {

        @Test
        public void nicknameが空の場合バリデーションエラーが発生する() {
            userForm.setNickname("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Nickname can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void emailが空の場合バリデーションエラーが発生する() {
            userForm.setEmail("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Email can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void profileが空の場合バリデーションエラーが発生する() {
            userForm.setProfile("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Profile can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void affiliationが空の場合バリデーションエラーが発生する() {
            userForm.setAffiliation("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Affiliation can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void positionが空の場合バリデーションエラーが発生する() {
            userForm.setPosition("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Position can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordが空の場合バリデーションエラーが発生する() {
            userForm.setPassword("");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            assertEquals(1, violations.size());
            assertEquals("Password can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordとpasswordConfirmationが不一致ではバリデーションエラーが発生する() {
            userForm.setPasswordConfirmation("differentPassword");
            userForm.validatePasswordConfirmation(bindingResult);
            verify(bindingResult).rejectValue("passwordConfirmation", "error.user", "Password confirmation doesn't match Password");
        }

        @Test
        public void emailはアットマークを含まないとバリデーションエラーが発生する() {
            userForm.setEmail("invalidEmail");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Email should be valid", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordが5文字以下ではバリデーションエラーが発生する() {
            userForm.setPassword(faker.internet().password(1, 5));
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordが129文字以上ではバリデーションエラーが発生する() {
            userForm.setPassword(faker.internet().password(129, 256));
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1, violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());
        }

        // 已废弃: 用户名长度限制功能已移除
        // @Test
        // public void nicknameが7文字以上ではバリデーションエラーが発生する() {
        //     userForm.setNickname("TooLong");
        //     Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
        //     assertEquals(1, violations.size());
        //     assertEquals("Nickname is too long (maximum is 6 characters)", violations.iterator().next().getMessage());
        // }
    }
}
