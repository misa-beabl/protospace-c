package in.tech_camp.protospace_c;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // プロトタイプ画像アップロード用
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:src/main/resources/static/uploads/");

    // ユーザーアイコン画像アップロード用
    registry.addResourceHandler("/user_avatars/**")
            .addResourceLocations("file:src/main/resources/static/user_avatars/");
  }
}
