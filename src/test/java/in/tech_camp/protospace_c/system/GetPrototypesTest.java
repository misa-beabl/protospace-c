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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import in.tech_camp.protospace_c.ProtospaceCApplication;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.factory.PrototypeFactory;
import in.tech_camp.protospace_c.factory.UserFormFactory;
import in.tech_camp.protospace_c.form.UserForm;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.repository.UserRepository;

@ActiveProfiles("test")
@SpringBootTest(classes = ProtospaceCApplication.class)
@AutoConfigureMockMvc
public class GetPrototypesTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrototypeRepository prototypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private List<PrototypeEntity> prototypeEntities;
    private PrototypeEntity prototype;

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
        
        prototypeEntities = PrototypeFactory.createPrototypeEntityList(user);
        for (PrototypeEntity prototypeEntity : prototypeEntities) {
            prototypeRepository.insert(prototypeEntity);
        }
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
    void トップページにプロトタイプ一覧が表示される() throws Exception {
        List<PrototypeEntity> dbPrototypes = prototypeRepository.findAll();

        assertThat(dbPrototypes).isNotEmpty();

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            // プロトタイプ一覧がmodelに乗っているかを検証
            .andExpect(model().attributeExists("prototypes"))
            // prototypeEntitiesの件数と一致するか検証
            .andExpect(model().attribute("prototypes", org.hamcrest.Matchers.hasSize(dbPrototypes.size())));
    }

    @Test
    @WithUserDetails(
        value = TEST_EMAIL, 
        userDetailsServiceBeanName = "userAuthenticationService",
        setupBefore = TestExecutionEvent.TEST_EXECUTION
    )
    void プロトタイプが存在しない場合でもトップページが正しく表示される() throws Exception {
        // プロトタイプを全削除
        prototypeRepository.deleteAll();

        List<PrototypeEntity> dbPrototypes = prototypeRepository.findAll();
        assertThat(dbPrototypes).isEmpty();

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            // プロトタイプ一覧がmodelに存在し、サイズ0であること
            .andExpect(model().attributeExists("prototypes"))
            .andExpect(model().attribute("prototypes", org.hamcrest.Matchers.hasSize(0)));
    }
}