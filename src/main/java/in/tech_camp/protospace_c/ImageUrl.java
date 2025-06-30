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

  @Value("${comment.image.url}")
  private String commentImageUrl;

  public String getImageUrl(){
    return url;
  }

  public String getUserAvatarUrl(){
    return userAvatarUrl;
  }

  public String getCommentImageUrl(){
    return commentImageUrl;
  }
}