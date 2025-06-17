package in.tech_camp.protospace_c.entity;

import lombok.Data;

@Data
public class UserEntity {
  private Integer id;
  private String nickname;
  private String email;
  private String password;
  private String profile;
  private String affiliation;
  private String position;
  // private List<TweetEntity> tweets;
  // private List<CommentEntity> comments;
}
