package in.tech_camp.protospace_c.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.mock.web.MockMultipartFile;

import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.form.PrototypeForm;

public class PrototypeFactory {
  public static PrototypeForm createPrototypeForm() {
    PrototypeForm prototypeForm = new PrototypeForm();

    prototypeForm.setName("TestName");
    prototypeForm.setSlogan("TestSlogan");
    prototypeForm.setConcept("TestConcept");

    // --- ユニークなファイル名を生成 ---
    String uniqueFileName = UUID.randomUUID().toString() + "_test.jpg";

    // ダミーファイルを作成
    MockMultipartFile mockImage = new MockMultipartFile(
      "image",
      uniqueFileName,
      "image/jpeg",
      "fake image content".getBytes()
    );
    prototypeForm.setImage(mockImage);

    return prototypeForm;
  }

  public static List<PrototypeEntity> createPrototypeEntityList(UserEntity user) {
    List<PrototypeEntity> prototypeEntities = new ArrayList<>();

    PrototypeEntity prototypeEntity1 = new PrototypeEntity();
    prototypeEntity1.setId(1);
    prototypeEntity1.setUser(user);
    prototypeEntity1.setName("プロトタイプ１");
    prototypeEntity1.setSlogan("プロトタイプ１のスローガン");
    prototypeEntity1.setConcept("プロトタイプ１のコンセプト");
    prototypeEntity1.setImage("/uploads/1_test.jpg");

    PrototypeEntity prototypeEntity2 = new PrototypeEntity();
    prototypeEntity2.setId(2);
    prototypeEntity2.setUser(user);
    prototypeEntity2.setName("プロトタイプ２");
    prototypeEntity2.setSlogan("プロトタイプ２のスローガン");
    prototypeEntity2.setConcept("プロトタイプ２のコンセプト");
    prototypeEntity2.setImage("/uploads/2_test.jpg");

    prototypeEntities.add(prototypeEntity1);
    prototypeEntities.add(prototypeEntity2);

    return prototypeEntities;
  }
}