package in.tech_camp.protospace_c;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class ImageUrl {
  @Value("${image.url}")
  private String url;

  @Value("${user.avatar.url}")
  private String userAvatarUrl;

  public String getImageUrl(){
    return url;
  }

  public String getUserAvatarUrl(){
    return userAvatarUrl;
  }
}