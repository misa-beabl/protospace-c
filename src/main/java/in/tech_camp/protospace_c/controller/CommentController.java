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

import in.tech_camp.protospace_c.ImageUrl;
import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.CommentEntity;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.form.CommentForm;
import in.tech_camp.protospace_c.repository.CommentRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.utils.CommentUtil;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import jakarta.servlet.http.HttpServletResponse;
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
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("formAction", "/prototypes/" + prototypeId + "/comment");
      model.addAttribute("submitLabel", "SEND");
      model.addAttribute("commentId", null);
      return "fragments/commentForm :: commentFormFragment";
    }

    CommentEntity comment = new CommentEntity();
    comment.setText(commentForm.getText());
    comment.setPrototype(prototype);
    comment.setUser(currentUser.getUser());
    if(commentForm.getParentId() != null) {
      comment.setParentComment(commentRepository.findById(commentForm.getParentId()));
    }
    
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
        errorMessages.add(new ObjectError("globalError", "コメントの保存に失敗しました"));
        model.addAttribute("errorMessages", errorMessages);
        model.addAttribute("commentForm", commentForm);
        model.addAttribute("formAction", "/prototypes/" + prototypeId + "/comment");
        model.addAttribute("submitLabel", "SEND");
        model.addAttribute("commentId", null);
        return "fragments/commentForm :: commentFormFragment";
      }
    }

    try {
      commentRepository.insert(comment);
      CommentEntity savedComment = commentRepository.findById(comment.getId());
      model.addAttribute("comments", List.of(savedComment));
      model.addAttribute("prototype", savedComment.getPrototype());
      model.addAttribute("prototypeId", savedComment.getPrototype().getId());
      return "fragments/commentTree :: allTreeFragment";
    } catch (Exception e) {
      List<ObjectError> errorMessages = new ArrayList<>();
      errorMessages.add(new ObjectError("globalError", "コメントの保存に失敗しました"));
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("formAction", "/prototypes/" + prototypeId + "/comment");
      model.addAttribute("submitLabel", "SEND");
      model.addAttribute("commentId", null);
      return "fragments/commentForm :: commentFormFragment";
    }
  }

  @GetMapping("/prototypes/{prototypeId}/comment-form-fragment")
  public String getEmptyCommentForm(
          @PathVariable("prototypeId") Integer prototypeId, Model model) {
      model.addAttribute("commentForm", new CommentForm());
      model.addAttribute("formAction", "/prototypes/" + prototypeId + "/comment");
      model.addAttribute("submitLabel", "SEND");
      model.addAttribute("commentId", null);
      return "fragments/commentForm :: commentFormFragment";
  }

  @GetMapping("/prototypes/{prototypeId}/comment-reply-form-fragment/{parentCommentId}")
  public String getReplyFormFragment(
        @PathVariable("prototypeId") Integer prototypeId,
        @PathVariable("parentCommentId") Integer parentCommentId,
        Model model) {
      CommentForm commentForm = new CommentForm();
      commentForm.setParentId(parentCommentId);
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("formAction", "/prototypes/" + prototypeId + "/comment");
      model.addAttribute("submitLabel", "SEND");
      model.addAttribute("commentId", parentCommentId);
      return "fragments/commentForm :: commentFormFragment";
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
  public String editComment(
    @PathVariable("commentId") Integer commentId,
    @ModelAttribute @Validated(ValidationOrder.class) CommentForm commentForm,
    BindingResult result,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    Model model,
    HttpServletResponse response
  ) {
    CommentEntity commentToEdit = commentRepository.findById(commentId);
    if (!commentToEdit.getUser().getId().equals(currentUser.getUser().getId())) {
        response.setStatus(403);
        return null;
    }

    if (result.hasErrors()) {
        model.addAttribute("errorMessages", result.getAllErrors());
        model.addAttribute("commentForm", commentForm);
        model.addAttribute("formAction", "/comment/" + commentId + "/edit");
        model.addAttribute("submitLabel", "UPDATE");
        model.addAttribute("commentId", commentId);
        return "fragments/commentForm :: commentFormFragment";
    }

    try {
      commentToEdit.setText(commentForm.getText());
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
          commentToEdit.setImage("/comment_images/" + fileName);
        } catch (IOException e) {
          result.rejectValue("image", "upload", "画像の保存に失敗しました");
          List<ObjectError> errorMessages = new ArrayList<>();
          errorMessages.add(new ObjectError("globalError", "コメントの保存に失敗しました"));
          model.addAttribute("errorMessages", errorMessages);
          model.addAttribute("commentForm", commentForm);
          model.addAttribute("formAction", "/comment/" + commentId + "/edit");
          model.addAttribute("submitLabel", "UPDATE");
          model.addAttribute("commentId", commentId);
          return "fragments/commentForm :: commentFormFragment";
        }
      }
      commentRepository.update(commentToEdit);

      CommentEntity updatedComment = commentRepository.findById(commentId);
      model.addAttribute("comment", updatedComment);
      model.addAttribute("prototype", updatedComment.getPrototype());
      model.addAttribute("prototypeId", updatedComment.getPrototype().getId());
      // return "fragments/commentTree :: allTreeFragment";
      return "fragments/commentItem :: commentItemFragment";
    } catch (Exception e) {
      List<ObjectError> errorMessages = new ArrayList<>();
      errorMessages.add(new ObjectError("globalError", "コメントの編集に失敗しました"));
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("commentForm", commentForm);
      model.addAttribute("formAction", "/comment/" + commentId + "/edit");
      model.addAttribute("submitLabel", "UPDATE");
      model.addAttribute("commentId", commentId);
      return "fragments/commentForm :: commentFormFragment";
    }
  }

  @GetMapping("/comment/{commentId}/edit-form")
  public String getEditCommentForm(
      @PathVariable("commentId") Integer commentId,
      Model model
  ) {
      CommentEntity comment = commentRepository.findById(commentId);
      CommentForm commentForm = new CommentForm();
      commentForm.setText(comment.getText());
      commentForm.setImage(null);
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

  @GetMapping("/prototypes/{prototypeId}/comments-tree")
  public String getCommentsTree(@PathVariable("prototypeId") Integer prototypeId, Model model) {
      List<CommentEntity> allComments = commentRepository.findByPrototypeId(prototypeId);
      List<CommentEntity> commentTree = CommentUtil.buildCommentTree(allComments);
      model.addAttribute("comments", commentTree);
      return "fragments/allCommentTree :: allTreeFragment";
  }
}