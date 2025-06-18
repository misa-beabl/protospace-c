package in.tech_camp.protospace_c.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.ImageUrl;
import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.form.PrototypeForm;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import lombok.AllArgsConstructor;


@Controller
@AllArgsConstructor
public class PrototypeController {
  private final PrototypeRepository prototypeRepository;
  private final ImageUrl imageUrl;

  @GetMapping("/")
  public String showPrototypes(Model model) {
    List<PrototypeEntity> prototypes = prototypeRepository.findAll();
    model.addAttribute("prototypes", prototypes);
    return "index";
  }

  @GetMapping("/prototype/new")
  public String showPrototypeNew(Model model) {
      model.addAttribute("prototypeForm", new PrototypeForm());
      return "prototypes/new";
  }

  @GetMapping("/prototype/{prototypeId}/edit")
  public String showEditPrototype(
    @PathVariable("prototypeId") Integer prototypeId,
    Model model
  ) {
    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);

    PrototypeForm prototypeForm = new PrototypeForm();
    prototypeForm.setName(prototype.getName());
    prototypeForm.setSlogan(prototype.getSlogan());
    prototypeForm.setConcept(prototype.getConcept());

    model.addAttribute("prototypeForm", prototypeForm);
    model.addAttribute("prototypeId", prototypeId);
    return "prototypes/edit";
  }

  @PostMapping("/prototype")
  public String createPrototypes(
    @ModelAttribute("prototypeForm") @Validated(ValidationOrder.class) PrototypeForm prototypeForm,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    BindingResult result,
    Model model) {
      MultipartFile imageFile = prototypeForm.getImage();
      if (imageFile == null || imageFile.isEmpty()) {
        result.rejectValue("image", "required", "画像を添付してください");
      }

      if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("prototypeForm", prototypeForm);
      return "prototypes/new";
      }

      PrototypeEntity prototype = new PrototypeEntity();
      // userが存在するか確認する（UserRepositoryにメソッド追加）
      prototype.setUser(currentUser.getUser());
      prototype.setName(prototypeForm.getName());
      prototype.setSlogan(prototypeForm.getSlogan());
      prototype.setConcept(prototypeForm.getConcept());

      try {
        String uploadDir = imageUrl.getImageUrl();
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + imageFile.getOriginalFilename();
        Path imagePath = Paths.get(uploadDir, fileName);
        Files.copy(imageFile.getInputStream(), imagePath);
        prototype.setImage("/uploads/" + fileName);
      } catch (IOException e) {
        result.rejectValue("image", "upload", "画像の保存に失敗しました");
        model.addAttribute("prototypeForm", prototypeForm);
        return "prototypes/new";
      }

      try {
        prototypeRepository.insert(prototype);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        return "redirect:/";
      }

      return "redirect:/";
  }

  // 編集機能
  @PostMapping("/prototypes/{prototypeId}/update")
  public String editPrototype(
    @ModelAttribute("prototypeForm") @Validated PrototypeForm prototypeForm,
    BindingResult result,
    @PathVariable("prototypeId") Integer prototypeId,
    Model model
  ) {
    MultipartFile imageFile = prototypeForm.getImage();
    if (imageFile == null || imageFile.isEmpty()) {
      result.rejectValue("image", "required", "画像を添付してください");
    }

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);

      model.addAttribute("prototypeForm", prototypeForm);
      model.addAttribute("prototypeId", prototypeId);
      return "prototypes/edit";
    }
    
    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);
    prototype.setName(prototypeForm.getName());
    prototype.setConcept(prototypeForm.getConcept());

    try {
      String uploadDir = imageUrl.getImageUrl();
      String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + imageFile.getOriginalFilename();
      Path imagePath = Paths.get(uploadDir, fileName);
      Files.copy(imageFile.getInputStream(), imagePath);
      prototype.setImage("/uploads/" + fileName);
    } catch (IOException e) {
      System.out.println("エラー：" + e);
      return "prototypes/edit";
    }

    try {
      prototypeRepository.insert(prototype);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "prototypes/edit";
    }

    return "prototypes/detail";
  }

  // 削除機能
  @PostMapping("/prototype/{prototypeId}/delete")
  public String deletePrototype(@PathVariable("prototypeId") Integer prototypeId) {
    try {
      prototypeRepository.deleteById(prototypeId);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "redirect:/";
    }
    return "redirect:/";
  }
}
