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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.ImageUrl;
import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.CommentEntity;
import in.tech_camp.protospace_c.entity.GenreEntity;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.form.CommentForm;
import in.tech_camp.protospace_c.form.PrototypeForm;
import in.tech_camp.protospace_c.form.SearchForm;
import in.tech_camp.protospace_c.repository.GenreRepository;
import in.tech_camp.protospace_c.repository.LikesRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.repository.UserRepository;
import in.tech_camp.protospace_c.utils.CommentUtil;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import lombok.AllArgsConstructor;



@Controller
@AllArgsConstructor
public class PrototypeController {
  private final PrototypeRepository prototypeRepository;
  private final UserRepository userRepository;
  private final ImageUrl imageUrl;
  private final LikesRepository likesRepository;
  private final GenreRepository genreRepository;

  @ModelAttribute("user")
  public UserEntity addUserToModel(@AuthenticationPrincipal CustomUserDetail currentUser) {
    if (currentUser != null) {
      return userRepository.findById(currentUser.getUser().getId());
    }
    return null;
  }

  @GetMapping("/")
  public String showPrototypes(@AuthenticationPrincipal CustomUserDetail currentUser, Model model) {
    List<PrototypeEntity> prototypes = prototypeRepository.findAll();
    SearchForm searchForm = new SearchForm();
    model.addAttribute("prototypes", prototypes);
    model.addAttribute("searchForm", searchForm);
    List<GenreEntity> genres = genreRepository.findAll();
    model.addAttribute("genres", genres);
    model.addAttribute("selectedGenreNames", new ArrayList<GenreEntity>());
    if (currentUser != null) {
      UserEntity user = userRepository.findById(currentUser.getUser().getId());
      // model.addAttribute("user", currentUser.getUser());
      model.addAttribute("user", user);

      List<Integer> likedIds = likesRepository.findLikedPrototypeIdsByUserId(currentUser.getUser().getId());
      model.addAttribute("likedIds", likedIds);
    }
    return "index";
  }

  @GetMapping("/prototype/search")
  public String searchTweets(@ModelAttribute("searchForm") SearchForm searchForm, Model model) {
    List<PrototypeEntity> prototypes = prototypeRepository.findByTextContaining(searchForm.getText());
    model.addAttribute("prototypes", prototypes);
    // 这里边表示，在展示搜索结果的页面中，搜索栏仍旧应当存在
    model.addAttribute("searchForm", searchForm);
    return "prototypes/search";
  }

  // ジャンル別のプロトタイプ検索
  @GetMapping("/prototype/genre")
  public String searchByGenre(@RequestParam(value = "genreId", required = false) List<Long> genreIds,
  @AuthenticationPrincipal CustomUserDetail currentUser, Model model) {
    List<PrototypeEntity> prototypes;
     if (genreIds == null || genreIds.isEmpty()) {
        prototypes = prototypeRepository.findAll();//選択されてないときは全プロトタイプの表示
    } else {
      prototypes = prototypeRepository.findByGenreIdIn(genreIds);//選択されたプロトタイプの表示
    }

    
    if (currentUser != null) {
      model.addAttribute("user", currentUser.getUser());
      List<Integer> likedIds = likesRepository.findLikedPrototypeIdsByUserId(currentUser.getUser().getId());
      model.addAttribute("likedIds", likedIds);
    } else {
      model.addAttribute("user", null);
      model.addAttribute("likedIds", new ArrayList<>());
    }

    List<GenreEntity> genres = genreRepository.findAll();

    List<GenreEntity> genreResults = new ArrayList<>();
    for (Long genreId : genreIds) {
        genreResults.add(genreRepository.findByLong(genreId));
    }

    model.addAttribute("prototypes", prototypes);
    model.addAttribute("genres", genres);//検索語もチェックボックスの表示
    model.addAttribute("selectedGenreIds", genreIds != null ? genreIds : new ArrayList<Long>());
    model.addAttribute("genreResults", genreResults);
    return "prototypes/genre";
  }
  

  @GetMapping("/prototype/new")
  public String showPrototypeNew(Model model) {
      List<GenreEntity> genres = genreRepository.findAll();
      model.addAttribute("genres", genres);
      model.addAttribute("prototypeForm", new PrototypeForm());
      return "prototypes/new";
  }

  @GetMapping("/prototype/{prototypeId}/edit")
  public String showEditPrototype(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    Model model
  ) {
    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);
    if (!currentUser.getUser().getId().equals(prototype.getUser().getId())) {
        return "redirect:/prototype/" + prototypeId;
    }

    Integer id = prototype.getGenre().getId();

    PrototypeForm prototypeForm = new PrototypeForm();
    prototypeForm.setName(prototype.getName());
    prototypeForm.setSlogan(prototype.getSlogan());
    prototypeForm.setConcept(prototype.getConcept());
    prototypeForm.setGenreId(id);

    List<GenreEntity> genres = genreRepository.findAll();
     
    model.addAttribute("genres", genres);
    model.addAttribute("prototype", prototype);
    model.addAttribute("prototypeForm", prototypeForm);
    model.addAttribute("prototypeId", prototypeId);
    return "prototypes/edit";
  }

  @PostMapping("/prototype")
  public String createPrototypes(
    @ModelAttribute("prototypeForm") @Validated(ValidationOrder.class) PrototypeForm prototypeForm,
    BindingResult result,
    @AuthenticationPrincipal CustomUserDetail currentUser,
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

      GenreEntity genre = genreRepository.findById(prototypeForm.getGenreId());

      PrototypeEntity prototype = new PrototypeEntity();
      // userが存在するか確認する（UserRepositoryにメソッド追加）
      prototype.setUser(currentUser.getUser());
      prototype.setName(prototypeForm.getName());
      prototype.setSlogan(prototypeForm.getSlogan());
      prototype.setConcept(prototypeForm.getConcept());
      prototype.setGenre(genre);    
     

      try {
        String uploadDir = imageUrl.getImageUrl();
        Path uploadDirPath = Paths.get(uploadDir);
        if (!Files.exists(uploadDirPath)) {
          Files.createDirectories(uploadDirPath);
        }
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + imageFile.getOriginalFilename();
        Path imagePath = Paths.get(uploadDir, fileName);
        Files.copy(imageFile.getInputStream(), imagePath);
        prototype.setImage("/uploads/" + fileName);
      } catch (IOException e) {
        result.rejectValue("image", "upload", "画像の保存に失敗しました");
        List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        model.addAttribute("errorMessages", errorMessages);
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
  @PostMapping("/prototype/{prototypeId}/update")
  public String editPrototype(
    @ModelAttribute("prototypeForm") @Validated(ValidationOrder.class) PrototypeForm prototypeForm,
    BindingResult result,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    @PathVariable("prototypeId") Integer prototypeId,
    Model model
  ) {

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);

      model.addAttribute("prototypeForm", prototypeForm);
      model.addAttribute("prototypeId", prototypeId);
      return "prototypes/edit";
    }

    GenreEntity genre = genreRepository.findById(prototypeForm.getGenreId());
    
    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);
    prototype.setUser(currentUser.getUser());
    prototype.setName(prototypeForm.getName());
    prototype.setSlogan(prototypeForm.getSlogan());
    prototype.setConcept(prototypeForm.getConcept());
    prototype.setGenre(genre);

    MultipartFile imageFile = prototypeForm.getImage();
    if (imageFile != null && !imageFile.isEmpty()) {
      try {
        String uploadDir = imageUrl.getImageUrl();
        Path uploadDirPath = Paths.get(uploadDir);
        if (!Files.exists(uploadDirPath)) {
          Files.createDirectories(uploadDirPath);
        }
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + imageFile.getOriginalFilename();
        Path imagePath = Paths.get(uploadDir, fileName);
        Files.copy(imageFile.getInputStream(), imagePath);
        prototype.setImage("/uploads/" + fileName);
      } catch (IOException e) {
        System.out.println("エラー：" + e);
        return "prototypes/edit";
      }
    }

    try {
      prototypeRepository.update(prototype);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "prototypes/edit";
    }

    return "redirect:/prototype/" + prototypeId;
  }

  // 削除機能
  @PostMapping("/prototype/{prototypeId}/delete")
  @ResponseBody
  public Map<String, Object> deletePrototype(@PathVariable("prototypeId") Integer prototypeId) {
    Map<String, Object> res = new HashMap<>();
    try {
      prototypeRepository.deleteById(prototypeId);
      res.put("success", true);
      res.put("redirectUrl", "/");
    } catch (Exception e) {
      res.put("success", false);
      res.put("error", e.getMessage());
    }
    return res;
  }

  @GetMapping("/prototype/{prototypeId}")
  public String showPrototypeDetail(
    @PathVariable("prototypeId") Integer prototypeId,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    Model model
  ) {
    PrototypeEntity prototype = prototypeRepository.findById(prototypeId);

    List<CommentEntity> comments = prototype.getComments();
    List<CommentEntity> commentTree = CommentUtil.buildCommentTree(comments);

    model.addAttribute("prototype", prototype);
    model.addAttribute("commentForm", new CommentForm());
    model.addAttribute("comments", commentTree);
    if (currentUser != null) {
        UserEntity user = userRepository.findById(currentUser.getUser().getId());
        // model.addAttribute("user", currentUser.getUser());
        model.addAttribute("user", user);

        List<Integer> likedIds = likesRepository.findLikedPrototypeIdsByUserId(currentUser.getUser().getId());
        model.addAttribute("likedIds", likedIds);
    }
    return "prototypes/detail";
  }
}
