package in.tech_camp.protospace_c.entity;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class DirectMessageEntity {
  private Integer id;
  private String text;
  private UserEntity senderUser;
  private UserEntity receiverUser;
  private Timestamp sentAt;
}
