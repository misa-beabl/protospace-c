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
        model.addAttribute("comments", prototype.getComments());
        if (currentUser != null) {
          model.addAttribute("user", currentUser.getUser());
        }
    }

    CommentEntity comment = new CommentEntity();
    comment.setText(commentForm.getText());
    comment.setPrototype(prototype);
    comment.setUser(currentUser.getUser());

    try {
      commentRepository.insert(comment);
      CommentEntity savedComment = commentRepository.findById(comment.getId());
      model.addAttribute("comment", savedComment);
      model.addAttribute("prototype", savedComment.getPrototype());
      return "fragments/commentItem :: commentItemFragment";
    } catch (Exception e) {
      List<String> errorMessages = new ArrayList<>();
      errorMessages.add("コメントの保存に失敗しました");
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("prototype", prototype);
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("comments", prototype.getComments());
      model.addAttribute("user", currentUser.getUser());
    }
  }

  @PostMapping("/prototype/{prototypeId}/comments/{commentId}/delete")
  public String deleteComment(@PathVariable("commentId") Integer commentId,
    @PathVariable("prototypeId") Integer prototypeId,
    Model model){

      model.addAttribute("commentId", commentId);

      try {
        commentRepository.deleteById(commentId);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        return "fragments/commentItem :: commentItemFragment";
      }   
      return "fragments/commentItem :: commentItemFragment";
  }
  
}