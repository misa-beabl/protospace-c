package in.tech_camp.protospace_c.entity;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class LikesEntity {
  private Integer id;
  private Integer userId;
  private Integer prototypeId;
  private Timestamp createdAt;
}
