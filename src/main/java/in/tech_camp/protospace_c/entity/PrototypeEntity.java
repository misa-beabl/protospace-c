package in.tech_camp.protospace_c.entity;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;
import lombok.ToString;


@Data
public class PrototypeEntity {
  private Integer id;
  private String name;
  private String slogan;
  private String concept;
  private String image;
  private Timestamp createdAt;
  private Integer likeCount;
  @ToString.Exclude
  private UserEntity user;
  @ToString.Exclude
  private List<CommentEntity> comments;
}
