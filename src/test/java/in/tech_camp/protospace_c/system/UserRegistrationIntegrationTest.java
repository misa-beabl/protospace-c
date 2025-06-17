package in.tech_camp.protospace_c.system;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;    
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; 
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view; 

import in.tech_camp.protospace_c.ProtospaceCApplication;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.factory.UserFormFactory;
import in.tech_camp.protospace_c.form.UserForm;
import in.tech_camp.protospace_c.repository.UserRepository;


@ActiveProfiles("test")
@SpringBootTest(classes = ProtospaceCApplication.class)
@AutoConfigureMockMvc
public class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private UserForm userForm;

    @Autowired
    private UserRepository userRepository;

    private int initialCount;
    private int afterCount;

    @BeforeEach
    public void setup() {
        userForm = UserFormFactory.createUser();
    }

    @Nested
    class ユーザー新規登録ができるとき {
        @Test
        void 正しい情報を入力すればユーザー新規登録ができてトップページに移動する() throws Exception {
            // 主页响应测试（如有主页及模板）
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk());
                    // .andExpect(view().name("tweets/index"));

            // 测试跳转到注册页面
            mockMvc.perform(get("/users/sign_up"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("users/signUp"));

            // 检查注册前用户数量
            List<UserEntity> userBefore = userRepository.findAll();
            initialCount = userBefore.size();

            // 提交注册表单，POST字段补全
            mockMvc.perform(post("/user")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("nickname", userForm.getNickname())
                    .param("email", userForm.getEmail())
                    .param("profile", userForm.getProfile())
                    .param("affiliation", userForm.getAffiliation())
                    .param("position", userForm.getPosition())
                    .param("password", userForm.getPassword())
                    .param("passwordConfirmation", userForm.getPasswordConfirmation())
                    .with(csrf()))
                    .andExpect(redirectedUrl("/"))
                    .andExpect(status().isFound());

            List<UserEntity> userAfter = userRepository.findAll();
            afterCount = userAfter.size();
            assertEquals(initialCount + 1, afterCount);
        }
    }

    @Nested
    class ユーザー新規登録ができないとき {
        // @Test
        // void 必須項目が空の場合バリデーションエラーになり登録されずサインアップページに戻る() throws Exception {
        //     // 忘记填写 Position
        //     mockMvc.perform(post("/user")
        //             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        //             .param("nickname", userForm.getNickname())
        //             .param("email", userForm.getEmail())
        //             .param("profile", userForm.getProfile())
        //             .param("affiliation", userForm.getAffiliation())
        //             .param("position", "") // 故意留空
        //             .param("password", userForm.getPassword())
        //             .param("passwordConfirmation", userForm.getPasswordConfirmation())
        //             .with(csrf()))
        //             .andExpect(view().name("users/signUp"))
        //             .andExpect(status().isOk())  // 重新显示注册页面
        //             .andExpect(content().string(org.hamcrest.Matchers.containsString("Position can't be blank")));
            
        //     // 校验没被插入
        //     List<UserEntity> userAfter = userRepository.findAll();
        //     afterCount = userAfter.size();
        //     // 数量不应改变
        //     assertEquals(initialCount, afterCount);
        // }

        @Test
        void メールアドレスが既に登録済みならエラーになる() throws Exception {
            // 预先插入一个用户
            UserEntity existUser = new UserEntity();
            existUser.setNickname("existUser");
            existUser.setEmail(userForm.getEmail()); // 用和即将测试的一样的email
            existUser.setPassword(userForm.getPassword());
            existUser.setProfile(userForm.getProfile());
            existUser.setAffiliation(userForm.getAffiliation());
            existUser.setPosition(userForm.getPosition());
            userRepository.insert(existUser);

            // 再用同email尝试注册
            mockMvc.perform(post("/user")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("nickname", userForm.getNickname())
                    .param("email", userForm.getEmail()) // same email
                    .param("profile", userForm.getProfile())
                    .param("affiliation", userForm.getAffiliation())
                    .param("position", userForm.getPosition())
                    .param("password", userForm.getPassword())
                    .param("passwordConfirmation", userForm.getPasswordConfirmation())
                    .with(csrf()))
                    .andExpect(view().name("users/signUp"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Email already exists")));
        }
    }
}
