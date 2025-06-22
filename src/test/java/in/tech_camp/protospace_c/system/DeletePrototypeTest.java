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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
public class DeletePrototypeTest {
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
    void プロトタイプを削除するとDBから削除されトップページにリダイレクトされる() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        Integer prototypeId = prototype.getId();

        mockMvc.perform(multipart("/prototype/" + prototypeId + "/delete")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        // 削除されたことを確認
        PrototypeEntity deleted = prototypeRepository.findById(prototypeId);
        assertThat(deleted).isNull();

        // カラム数が減っているか検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size() - 1);
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL,
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void 存在しないIDを指定するとリダイレクトされDBの内容は変わらない() throws Exception {
        List<PrototypeEntity> before = prototypeRepository.findAll();

        // 存在しないID
        Integer notExistId = prototype.getId() + 1000;

        mockMvc.perform(multipart("/prototype/" + notExistId + "/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));

        // DBの内容に変化がないことを検証
        List<PrototypeEntity> after = prototypeRepository.findAll();
        assertThat(after).hasSize(before.size());
    }
}