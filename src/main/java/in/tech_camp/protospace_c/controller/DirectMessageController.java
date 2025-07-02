package in.tech_camp.protospace_c.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.DirectMessageEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.form.MessageForm;
import in.tech_camp.protospace_c.repository.DirectMessageRepository;
import in.tech_camp.protospace_c.repository.UserRepository;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class DirectMessageController {
  private final DirectMessageRepository directMessageRepository;
  private final UserRepository userRepository;

  @ModelAttribute("user")
  public UserEntity addUserToModel(@AuthenticationPrincipal CustomUserDetail currentUser) {
    if (currentUser != null) {
      return userRepository.findById(currentUser.getUser().getId());
    }
    return null;
  }

  @GetMapping("/messages/{userId}")
  public String showDmView(
    @PathVariable("userId") Integer userId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    Model model,
    @ModelAttribute("messageForm") MessageForm messageForm
  ) {
      // ログインユーザー
      UserEntity current = userRepository.findById(currentUser.getUser().getId());
      // DM相手
      UserEntity partner = userRepository.findById(userId);

      // 2ユーザー間のDM履歴取得
      List<DirectMessageEntity> dmList = directMessageRepository.findBetweenUsers(current.getId(), partner.getId());

      model.addAttribute("currentUser", current);
      model.addAttribute("partnerUser", partner);
      model.addAttribute("dmList", dmList);
      return "messages/message";
  }

  @PostMapping("/messages/{userId}")
  public String sendDm(
          @PathVariable("userId") Integer userId,
          @AuthenticationPrincipal CustomUserDetail currentUser,
          @ModelAttribute("messageForm") @Validated(ValidationOrder.class) MessageForm messageForm,
          BindingResult result,
          Model model
  ) {
      UserEntity sender = userRepository.findById(currentUser.getUser().getId());
      UserEntity receiver = userRepository.findById(userId);

      if (result.hasErrors()) {
        List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        model.addAttribute("errorMessages", errorMessages);

        // 必要な情報を再度セット
        List<DirectMessageEntity> dmList = directMessageRepository.findBetweenUsers(sender.getId(), receiver.getId());

        model.addAttribute("currentUser", sender);
        model.addAttribute("partnerUser", receiver);
        model.addAttribute("dmList", dmList);

        return "messages/message";
    }

      // 登録用Entityにデータを詰める
      DirectMessageEntity dm = new DirectMessageEntity();
      dm.setSenderUser(sender);
      dm.setReceiverUser(receiver);
      dm.setText(messageForm.getText());
      directMessageRepository.insert(dm);

      return "redirect:/messages/" + userId;
  }
}