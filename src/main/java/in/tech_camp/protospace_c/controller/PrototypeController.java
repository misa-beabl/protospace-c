package in.tech_camp.protospace_c.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AllArgsConstructor;



@Controller
@AllArgsConstructor
public class PrototypeController {

  @GetMapping("/prototype/new")
  public String showPrototypeNew(
    // @AuthenticationPrincipal CustomUserDetail currentUser, 
    Model model
    ) {
      return "prototypes/new";
  }

  // パスは後で変更
  @GetMapping("/prototype")
  public String editPrototype() {
      return "prototypes/edit";
  }

}
