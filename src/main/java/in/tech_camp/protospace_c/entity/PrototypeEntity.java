package in.tech_camp.protospace_c.entity;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PrototypeEntity {
  private Integer id;
  private Integer user_id;
  private String name;
  private String slogan;
  private String concept;
  private String image;
  private Timestamp createdAt;
}
