package in.tech_camp.protospace_c;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:src/main/resources/static/uploads/");
    registry.addResourceHandler("/comment_images/**")
            .addResourceLocations("file:src/main/resources/static/comment_images/");
    registry.addResourceHandler("/user_avatars/**")
            .addResourceLocations("file:src/main/resources/static/user_avatars/");
  }
}
