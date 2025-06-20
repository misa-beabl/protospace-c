package in.tech_camp.protospace_c.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.CommentEntity;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.form.CommentForm;
import in.tech_camp.protospace_c.repository.CommentRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class CommentController {

  private final CommentRepository commentRepository;

  private final PrototypeRepository prototypeRepository;

  // 评论投稿的post处理
  @PostMapping("/prototypes/{prototypeId}/comment")
  public String createComment(@PathVariable("prototypeId") Integer prototypeId, 
                            @ModelAttribute("commentForm") @Validated(ValidationOrder.class) CommentForm commentForm,
                            BindingResult result,
                            @AuthenticationPrincipal CustomUserDetail currentUser, Model model) {

    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);

    if (result.hasErrors()) {
        model.addAttribute("errorMessages", result.getAllErrors());
        model.addAttribute("prototype", prototype);
        model.addAttribute("commentForm", commentForm);
        return "prototypes/detail";
    }

    CommentEntity comment = new CommentEntity();
    comment.setText(commentForm.getText());
    comment.setPrototype(prototype);
    comment.setUser(currentUser.getUser());

    try {
      commentRepository.insert(comment);
    } catch (Exception e) {
      List<String> errorMessages = new ArrayList<>();
      errorMessages.add("コメントの保存に失敗しました");
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("prototype", prototype);
      model.addAttribute("commentForm", commentForm);
      System.out.println("エラー：" + e);
      return "prototypes/detail";
    }

    return "redirect:/prototypes/" + prototypeId;
  }
}