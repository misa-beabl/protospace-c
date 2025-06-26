package in.tech_camp.protospace_c.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
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
  @ResponseBody
  public Map<String, Object> LikeFromUserDetail(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    RedirectAttributes redirectAttributes
  ) {
    Integer userId = currentUser.getUser().getId();
    Map<String, Object> result = toggleLike(userId, prototypeId);
    return result;
  }

  // プロトタイプ詳細ページ用
  @PostMapping("/prototype/{prototypeId}/like-from-p-detail")
  @ResponseBody
  public Map<String, Object> LikeFromPrototypeDetail(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    RedirectAttributes redirectAttributes
  ) {
    Integer userId = currentUser.getUser().getId();
    Map<String, Object> result = toggleLike(userId, prototypeId);
    return result;
  }

  @GetMapping("/prototype/liked-prototypes")
  @ResponseBody
  public List<Map<String, Object>> getLikedPrototypes(
      @AuthenticationPrincipal CustomUserDetail currentUser
  ) {
      if (currentUser == null) {
          // 未ログインの場合は400や401など適切なエラーを返す
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ログインが必要です");
      }
      List<Integer> likedIds = likesRepository.findLikedPrototypeIdsByUserId(currentUser.getUser().getId());

      List<Map<String, Object>> result = likedIds.stream()
          .map(pid -> {
              PrototypeEntity p = prototypeRepository.findById(pid);
              if (p == null) return null;
              Map<String, Object> map = new HashMap<>();
              map.put("id", p.getId());
              map.put("name", p.getName());
              map.put("slogan", p.getSlogan());
              map.put("image", p.getImage());
              map.put("likeCount", p.getLikeCount());
              map.put("user", Map.of(
                  "id", p.getUser().getId(),
                  "nickname", p.getUser().getNickname()
              ));
              return map;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      return result;
  }
}