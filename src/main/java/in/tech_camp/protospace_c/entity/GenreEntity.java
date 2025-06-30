package in.tech_camp.protospace_c.entity;

import java.util.List;

import lombok.Data;

@Data
public class GenreEntity {
  private Integer id;
  private String genreName;
  private List<PrototypeEntity> prototypes;
}
