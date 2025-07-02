package in.tech_camp.protospace_c.entity;

import java.util.List;

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
  private String avatar;
  private List<PrototypeEntity> prototypes;
  private List<CommentEntity> comments;
  private List<DirectMessageEntity> directMessages;
}
