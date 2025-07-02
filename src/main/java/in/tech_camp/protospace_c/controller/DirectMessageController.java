package in.tech_camp.protospace_c.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.DirectMessageEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.repository.DirectMessageRepository;
import in.tech_camp.protospace_c.repository.UserRepository;
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
    Model model
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
      // 新規投稿用フォームオブジェクト
      model.addAttribute("newMessage", new DirectMessageEntity());

      return "messages/message";
  }
}
