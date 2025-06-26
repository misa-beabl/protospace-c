package in.tech_camp.protospace_c.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.repository.LikesRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class LikesController {
  private final LikesRepository likesRepository;
  private final PrototypeRepository prototypeRepository;

  @ModelAttribute("user")
  public UserEntity addUserToModel(@AuthenticationPrincipal CustomUserDetail currentUser) {
    if (currentUser != null) {
      return currentUser.getUser();
    }
    return null;
  }

  private Map<String, Object> toggleLike(Integer userId, Integer prototypeId) {
    // 既にいいね済みか判定（EXISTSはbooleanで返る）
    boolean alreadyLiked = likesRepository.exists(userId, prototypeId);
    boolean newLiked;

    if (alreadyLiked) {
      // 解除（delete）
      likesRepository.delete(userId, prototypeId);
      prototypeRepository.decrementLikeCount(prototypeId); // いいね数を減らす
      newLiked = false;
    } else {
      // 登録（insert）
      likesRepository.insert(userId, prototypeId);
      prototypeRepository.incrementLikeCount(prototypeId); // いいね数を増やす
      newLiked = true;
    }
    Integer likeCount = prototypeRepository.findById(prototypeId).getLikeCount();
    Map<String, Object> result = new HashMap<>();
        result.put("liked", newLiked);
        result.put("likeCount", likeCount);
    return result;
  }

  // トップページ用
  @PostMapping("/prototype/{prototypeId}/like-from-index")
  @ResponseBody
  public Map<String, Object> LikeFromIndex(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    RedirectAttributes redirectAttributes
  ) {
    Integer userId = currentUser.getUser().getId();
    Map<String, Object> result = toggleLike(userId, prototypeId);
    return result;
  }

  // ユーザー詳細ページ用
  @PostMapping("/prototype/{prototypeId}/like-from-u-detail")
  public String LikeFromUserDetail(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    RedirectAttributes redirectAttributes
  ) {
    Integer userId = currentUser.getUser().getId();
    toggleLike(userId, prototypeId);

    Integer pageUserId = prototypeRepository.findById(prototypeId).getUser().getId();
    return "redirect:/users/" + pageUserId;
  }

  // プロトタイプ詳細ページ用
  @PostMapping("/prototype/{prototypeId}/like-from-p-detail")
  public String LikeFromPrototypeDetail(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    RedirectAttributes redirectAttributes
  ) {
    Integer userId = currentUser.getUser().getId();
    toggleLike(userId, prototypeId);
    return "redirect:/prototype/" + prototypeId;
  }
}