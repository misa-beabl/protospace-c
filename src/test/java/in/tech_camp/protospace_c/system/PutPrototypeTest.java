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
public class PutPrototypeTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrototypeRepository prototypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private PrototypeForm prototypeForm;
    private PrototypeEntity prototype;
    private String uploadedFileName;

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
        prototype = new PrototypeEntity();
        prototype.setUser(user);
        prototype.setName(prototypeForm.getName());
        prototype.setSlogan(prototypeForm.getSlogan());
        prototype.setConcept(prototypeForm.getConcept());

        uploadedFileName = prototypeForm.getImage().getOriginalFilename();
        prototype.setImage("/uploads/" + uploadedFileName);
        prototypeRepository.insert(prototype);
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
    void 正しい情報を入力すればプロトタイプ編集ができる() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        Integer prototypeId = prototype.getId();

        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        mockMvc.perform(multipart("/prototypes/" + prototypeId + "/update")
            .file(imageFile)
            .param("name", "編集後の名前")
            .param("slogan", "編集後のスローガン")
            .param("concept", "編集後のコンセプト")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/prototypes/" + prototypeId));

        // カラム数に変化がないか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size());

        // 登録内容が正しいか検証
        PrototypeEntity updated = prototypeRepository.findById(prototypeId);
        assertThat(updated.getName()).isEqualTo("編集後の名前");
        assertThat(updated.getSlogan()).isEqualTo("編集後のスローガン");
        assertThat(updated.getConcept()).isEqualTo("編集後のコンセプト");
        assertThat(updated.getImage()).isNotNull();
    }

    @Test
    @WithUserDetails(
    value = TEST_EMAIL, 
    userDetailsServiceBeanName = "userAuthenticationService",
    setupBefore = TestExecutionEvent.TEST_EXECUTION
)
    void nameが未入力だと編集画面に戻りエラーメッセージが表示される() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        Integer prototypeId = prototype.getId();

        prototypeForm.setName("");

        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        mockMvc.perform(multipart("/prototypes/" + prototypeId + "/update")
            .file(imageFile)
            .param("name", prototypeForm.getName())
            .param("slogan", prototypeForm.getSlogan())
            .param("concept", prototypeForm.getConcept())
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/edit"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "name"));

        // カラム数に変化がないか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size());

        // 登録内容に変化がないか検証
        PrototypeEntity updated = prototypeRepository.findById(prototypeId);
        assertThat(updated.getName()).isEqualTo(prototype.getName());
        assertThat(updated.getSlogan()).isEqualTo(prototype.getSlogan());
        assertThat(updated.getConcept()).isEqualTo(prototype.getConcept());
        assertThat(updated.getImage()).isNotNull();
    }

    @Test
    @WithUserDetails(
    value = TEST_EMAIL, 
    userDetailsServiceBeanName = "userAuthenticationService",
    setupBefore = TestExecutionEvent.TEST_EXECUTION
)
    void sloganが未入力だと編集画面に戻りエラーメッセージが表示される() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        Integer prototypeId = prototype.getId();

        prototypeForm.setSlogan("");

        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        mockMvc.perform(multipart("/prototypes/" + prototypeId + "/update")
            .file(imageFile)
            .param("name", prototypeForm.getName())
            .param("slogan", prototypeForm.getSlogan())
            .param("concept", prototypeForm.getConcept())
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/edit"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "slogan"));

        // カラム数に変化がないか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size());

        // 登録内容に変化がないか検証
        PrototypeEntity updated = prototypeRepository.findById(prototypeId);
        assertThat(updated.getName()).isEqualTo(prototype.getName());
        assertThat(updated.getSlogan()).isEqualTo(prototype.getSlogan());
        assertThat(updated.getConcept()).isEqualTo(prototype.getConcept());
        assertThat(updated.getImage()).isNotNull();
    }

    @Test
    @WithUserDetails(
    value = TEST_EMAIL, 
    userDetailsServiceBeanName = "userAuthenticationService",
    setupBefore = TestExecutionEvent.TEST_EXECUTION
)
    void conceptが未入力だと編集画面に戻りエラーメッセージが表示される() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        Integer prototypeId = prototype.getId();

        prototypeForm.setConcept("");

        MockMultipartFile imageFile = (MockMultipartFile) prototypeForm.getImage();

        mockMvc.perform(multipart("/prototypes/" + prototypeId + "/update")
            .file(imageFile)
            .param("name", prototypeForm.getName())
            .param("slogan", prototypeForm.getSlogan())
            .param("concept", prototypeForm.getConcept())
            .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("prototypes/edit"))
            .andExpect(model().attributeHasFieldErrors("prototypeForm", "concept"));

        // カラム数に変化がないか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size());

        // 登録内容に変化がないか検証
        PrototypeEntity updated = prototypeRepository.findById(prototypeId);
        assertThat(updated.getName()).isEqualTo(prototype.getName());
        assertThat(updated.getSlogan()).isEqualTo(prototype.getSlogan());
        assertThat(updated.getConcept()).isEqualTo(prototype.getConcept());
        assertThat(updated.getImage()).isNotNull();
    }
}
