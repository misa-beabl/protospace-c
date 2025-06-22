package in.tech_camp.protospace_c.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import in.tech_camp.protospace_c.ProtospaceCApplication;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.factory.PrototypeFactory;
import in.tech_camp.protospace_c.factory.UserFormFactory;
import in.tech_camp.protospace_c.form.PrototypeForm;
import in.tech_camp.protospace_c.form.UserForm;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.repository.UserRepository;

@ActiveProfiles("test")
@SpringBootTest(classes = ProtospaceCApplication.class)
@AutoConfigureMockMvc
public class PostPrototypeTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrototypeRepository prototypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private PrototypeForm prototypeForm;

    private static final String TEST_EMAIL = "testuser@example.com";

    @BeforeEach
    void setup() {
        UserForm userForm = UserFormFactory.createUser();

        // ユーザーエンティティに変換し暗号化してinsert
        UserEntity user = new UserEntity();
        user.setEmail(TEST_EMAIL);
        user.setNickname(userForm.getNickname());
        user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        user.setProfile(userForm.getProfile());
        user.setAffiliation(userForm.getAffiliation());
        user.setPosition(userForm.getPosition());
        userRepository.insert(user);
        
        prototypeForm = PrototypeFactory.createPrototypeForm();
    }

    @AfterEach
    void cleanUp() throws IOException {
        userRepository.deleteAll();
        prototypeRepository.deleteAll();

        // 「_test.jpg」で終わるファイルをすべて削除
        Path uploadsDir = Path.of("src/main/resources/static/uploads");
        try (var stream = Files.newDirectoryStream(uploadsDir, "*_test.jpg")) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
    }

    @Test
    @WithUserDetails(
    value = TEST_EMAIL, 
    userDetailsServiceBeanName = "userAuthenticationService",
    setupBefore = TestExecutionEvent.TEST_EXECUTION
)
    void 正しい情報を入力すればプロトタイプ登録ができトップページにリダイレクトされる() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        mockMvc.perform(multipart("/prototype")
            .file(imageFile)
            .param("name", prototypeForm.getName())
            .param("slogan", prototypeForm.getSlogan())
            .param("concept", prototypeForm.getConcept())
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));

        // DBに1件増えているか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size() + 1);

        // 登録内容が正しいか検証
        PrototypeEntity last = after.get(0);
        assertThat(last.getName()).isEqualTo(prototypeForm.getName());
        assertThat(last.getSlogan()).isEqualTo(prototypeForm.getSlogan());
        assertThat(last.getConcept()).isEqualTo(prototypeForm.getConcept());
        assertThat(last.getImage()).isNotNull();
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL,
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void nameが未入力だと新規登録画面に戻りエラーメッセージが表示される() throws Exception {
        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        prototypeForm.setName("");

        mockMvc.perform(multipart("/prototype")
                .file(imageFile)
                .param("name", prototypeForm.getName())
                .param("slogan", prototypeForm.getSlogan())
                .param("concept", prototypeForm.getConcept())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/new"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "name"));
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL,
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void sloganが未入力だと新規登録画面に戻りエラーメッセージが表示される() throws Exception {
        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        prototypeForm.setSlogan("");

        mockMvc.perform(multipart("/prototype")
                .file(imageFile)
                .param("name", prototypeForm.getName())
                .param("slogan", prototypeForm.getSlogan())
                .param("concept", prototypeForm.getConcept())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/new"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "slogan"));
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL,
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void conceptが未入力だと新規登録画面に戻りエラーメッセージが表示される() throws Exception {
        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        prototypeForm.setConcept("");

        mockMvc.perform(multipart("/prototype")
                .file(imageFile)
                .param("name", prototypeForm.getName())
                .param("slogan", prototypeForm.getSlogan())
                .param("concept", prototypeForm.getConcept())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/new"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "concept"));
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL,
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void imageが未添付だと新規登録画面に戻りエラーメッセージが表示される() throws Exception {
        // 画像添付なし
        
        mockMvc.perform(multipart("/prototype")
                .param("name", prototypeForm.getName())
                .param("slogan", prototypeForm.getSlogan())
                .param("concept", prototypeForm.getConcept())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/new"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "image"));
    }
}
