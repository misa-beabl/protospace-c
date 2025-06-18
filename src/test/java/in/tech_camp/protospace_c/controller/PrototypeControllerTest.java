package in.tech_camp.protospace_c.controller;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import in.tech_camp.protospace_c.repository.PrototypeRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PrototypeControllerTest {
  
  @Mock
  private PrototypeRepository prototypeRepository;

  @InjectMocks
  private PrototypeController prototypeController;

  @Test
  public void 投稿機能にリクエストするとプロトタイプ投稿ビューがレスポンスされる() {
    Model model = new ExtendedModelMap();

    String result = prototypeController.showPrototypeNew(model);

    assertThat(result,is("prototypes/new"));
  }
}
