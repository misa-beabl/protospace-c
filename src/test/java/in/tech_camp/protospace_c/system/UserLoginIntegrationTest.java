package in.tech_camp.protospace_c.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import in.tech_camp.protospace_c.service.UserService;


@ActiveProfiles("test")
@SpringBootTest(classes = ProtospaceCApplication.class)
@AutoConfigureMockMvc
public class UserLoginIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  private UserForm userForm;

  @BeforeEach
  public void setup() {
    // テスト用のユーザー情報をセットアップ
    userForm = UserFormFactory.createUser();
    UserEntity userEntity = new UserEntity();
    userEntity.setEmail(userForm.getEmail());
    userEntity.setNickname(userForm.getNickname());
    userEntity.setPassword(userForm.getPassword());
    userEntity.setAffiliation(userForm.getAffiliation());
    userEntity.setProfile(userForm.getProfile());
    userEntity.setPosition(userForm.getPosition());
    userService.createUserWithEncryptedPassword(userEntity);
  }

  @Nested
  class ユーザーログインができるとき {
    @Test
    public void 保存されているユーザーの情報と合致すればログインができる() throws Exception {
      // 首先看主页能不能连上
      mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"))
        // 检查主页的模型内容中有没有 登录按钮
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ログイン")));

      // get 请求连接登录页，查看连接状态是不是 OK
      // 查看返回的 view 文件是不是 /users/login.html
      mockMvc.perform(get("/users/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/login"));

      // post请求模拟登陆后的操作，首先这个 post 的地址要和 controller 类里的对上号
      MvcResult loginResult = mockMvc.perform(post("/login")
        // 定义填入表单的内容
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        // 创建适当的用户名密码，传给表单
        .param("email", userForm.getEmail())
        .param("password", userForm.getPassword())
        // csrf 验证，模拟spring security 验证形式
        .with(csrf()))
        // 重定向状态码和重定向地址
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("/"))
        // .andReturn() 方法会从 MockMvc.perform() 执行的结果中，返回一个 MvcResult 对象。
        // 这个 MvcResult 对象包含了模拟请求的所有相关数据和执行结果，例如：
        // 响应内容（HTML、JSON、纯文本等），HTTP 响应头，响应状态码，重定向地址，执行时间，使用场景
        // 如果需要进一步验证或解析 HTTP 响应内容，可以通过 MvcResult 获取完整的响应数据。
        .andReturn();

      // MockHttpSession 是 Spring 提供的测试类，用于模拟 HttpSession 的行为。
      // loginResult.getRequest() 从 MvcResult 对象中获取 MockHttpServletRequest（Spring 提供的模拟 HttpServletRequest 对象）。
      // .getSession() 从请求对象中获取返回 MockHttpSession（Spring 提供的模拟 HttpSession 对象）。
      // (MockHttpSession) 将返回的会话对象强制转换为 MockHttpSession 类型，以便支持进一步的测试和验证。
      // 最终结果:
      // 成功提取了模拟登录请求中与会话关联的 MockHttpSession，可以用来验证或操作会话数据。
      MockHttpSession session = (MockHttpSession)loginResult.getRequest().getSession();

      // 因为此时用户是登陆状态，所以需要将登陆结果的 session 传给后边请求操作（因为登陆前后主页是不一样的）
      // 其他操作和验证主页连接状态基本相同，不过主页内容确认有所不同
      mockMvc.perform(get("/").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("index"));
        // 确定登陆后主页中有登出按钮
        // .andExpect(content().string(org.hamcrest.Matchers.containsString("logout-btn")))
        // // 确定登陆后主页中不再有注册按钮
        // .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("新規登録"))));
    }
  }

  @Nested
  class ユーザーログインができないとき{
    @Test
    public void 保存されているユーザーの情報と合致しないとログインができない() throws Exception {
      // トップページに移動する
      mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"))
        // トップページにログインページへ遷移するボタンがあることを確認する
        .andExpect(content().string(org.hamcrest.Matchers.containsString("ログイン")));
      // ログインページに遷移する
      mockMvc.perform(get("/users/login"))
        .andExpect(status().isOk())
        .andExpect(view().name("users/login"));

      // 間違ったユーザー情報でログインを試みる
      mockMvc.perform(post("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("username", "test")
        .param("password", "")
        .with(csrf()))
      // 验证登陆失败时的重定向地址是否正确
      .andExpect(redirectedUrl("/login?error"))
      .andExpect(status().isFound());
    }
  }
}