package in.tech_camp.protospace_c.entity;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class CommentEntity {
  private Integer id;
  private String text;
  private String image;
  private UserEntity user;
  private PrototypeEntity prototype;
  private List<CommentEntity> replyComments;
  private CommentEntity ParentComment;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}