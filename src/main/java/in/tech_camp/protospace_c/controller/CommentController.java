package in.tech_camp.protospace_c.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.ImageUrl;
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

  private final ImageUrl imageUrl;

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
    
    MultipartFile commentImage = commentForm.getImage();
    if (commentImage != null && !commentImage.isEmpty()) {
      try {
        String commentImageUrl = imageUrl.getCommentImageUrl();
        Path commentImageDir = Paths.get(commentImageUrl);
        if (!Files.exists(commentImageDir)) {
          Files.createDirectories(commentImageDir);
        }
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + commentImage.getOriginalFilename();
        Path commentImagePath = Paths.get(commentImageUrl, fileName);
        Files.copy(commentImage.getInputStream(), commentImagePath);
        comment.setImage("/comment_images/" + fileName);
      } catch (IOException e) {
        result.rejectValue("image", "upload", "画像の保存に失敗しました");
        List<ObjectError> errorMessages = new ArrayList<>();
        errorMessages.add(new ObjectError("globalError", "コメントの保存に失敗しました"));
        model.addAttribute("errorMessages", errorMessages);
        model.addAttribute("prototype", prototype);
        model.addAttribute("commentForm", commentForm);
        model.addAttribute("comments", prototype.getComments());
        model.addAttribute("user", currentUser.getUser());
        return "prototypes/detail";
      }
    }

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
      return "prototypes/detail";
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