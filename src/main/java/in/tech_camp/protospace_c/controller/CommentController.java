package in.tech_camp.protospace_c.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import in.tech_camp.protospace_c.ImageUrl;
import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.CommentEntity;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.form.CommentForm;
import in.tech_camp.protospace_c.repository.CommentRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class CommentController {

  private final CommentRepository commentRepository;
  private final PrototypeRepository prototypeRepository;
  private final ImageUrl imageUrl;
  private final SpringTemplateEngine templateEngine;

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
  @ResponseBody
  public Map<String, Object> deleteComment(
      @PathVariable("commentId") Integer commentId,
      @PathVariable("prototypeId") Integer prototypeId
  ) {
      Map<String, Object> res = new HashMap<>();
      try {
          commentRepository.deleteById(commentId);
          res.put("success", true);
          res.put("commentId", commentId);
      } catch (Exception e) {
          res.put("success", false);
          res.put("error", e.getMessage());
      }
      return res;
  }
  
  @PostMapping("/comment/{commentId}/edit")
  @ResponseBody
  public Map<String, Object> editComment(
    @PathVariable("commentId") Integer commentId,
    @ModelAttribute @Validated CommentForm commentForm,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    HttpServletRequest request
  ) {
    Map<String, Object> res = new HashMap<>();
    try {
        CommentEntity commentToEdit = commentRepository.findById(commentId);
        if (!commentToEdit.getUser().getId().equals(currentUser.getUser().getId())) {
          res.put("success", false);
          res.put("error", "編集権限がありません");
          return res;
        }
        commentToEdit.setText(commentForm.getText());
        MultipartFile commentImage = commentForm.getImage();
        if (commentImage != null && !commentImage.isEmpty()) {
          String commentImageUrl = imageUrl.getCommentImageUrl();
          Path commentImageDir = Paths.get(commentImageUrl);
          if (!Files.exists(commentImageDir)) {
            Files.createDirectories(commentImageDir);
          }
          String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + commentImage.getOriginalFilename();
          Path commentImagePath = Paths.get(commentImageUrl, fileName);
          Files.copy(commentImage.getInputStream(), commentImagePath);
          commentToEdit.setImage("/comment_images/" + fileName);
        }
        commentRepository.update(commentToEdit);

        CommentEntity updatedComment = commentRepository.findById(commentId);
        Map<String, Object> model = new HashMap<>();
        model.put("comment", updatedComment);
        model.put("prototype", updatedComment.getPrototype());
        model.put("editCommentForm", commentForm); // 用于表单回填
        model.put("prototypeId", updatedComment.getPrototype().getId());

        Context ctx = new Context();
        ctx.setVariables(model);
        String html = templateEngine.process("fragments/commentItem :: commentItemFragment", ctx);

        res.put("success", true);
        res.put("commentId", updatedComment.getId());
        res.put("html", html);
    } catch (Exception e) {
        res.put("success", false);
        res.put("error", "コメントの編集に失敗しました");
    }
    return res;
  }

  @GetMapping("/comment/{commentId}/edit-form")
  public String getEditCommentForm(
      @PathVariable("commentId") Integer commentId,
      Model model
  ) {
      CommentEntity comment = commentRepository.findById(commentId);
      CommentForm commentForm = new CommentForm();
      commentForm.setText(comment.getText());
      commentForm.setImage(null); // 不回填图片
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("formAction", "/comment/" + commentId + "/edit");
      model.addAttribute("submitLabel", "UPDATE");
      model.addAttribute("commentId", commentId);
      return "fragments/commentForm :: commentFormFragment";
  }

  @GetMapping("/comment/{commentId}/item")
  public String getCommentItem(
      @PathVariable("commentId") Integer commentId,
      Model model
  ) {
      CommentEntity comment = commentRepository.findById(commentId);
      model.addAttribute("comment", comment);
      model.addAttribute("prototype", comment.getPrototype());
      model.addAttribute("prototypeId", comment.getPrototype().getId());
      return "fragments/commentItem :: commentItemFragment";
  }
}