package in.tech_camp.protospace_c.factory;

import java.util.UUID;

import org.springframework.mock.web.MockMultipartFile;

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
}