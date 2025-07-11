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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import in.tech_camp.protospace_c.ImageUrl;
import in.tech_camp.protospace_c.custom_user.CustomUserDetail;
import in.tech_camp.protospace_c.entity.DirectMessageEntity;
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.form.UserForm;
import in.tech_camp.protospace_c.form.UserIconForm;
import in.tech_camp.protospace_c.repository.DirectMessageRepository;
import in.tech_camp.protospace_c.repository.LikesRepository;
import in.tech_camp.protospace_c.repository.PrototypeRepository;
import in.tech_camp.protospace_c.repository.UserRepository;
import in.tech_camp.protospace_c.service.UserService;
import in.tech_camp.protospace_c.validation.ValidationOrder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;



@Controller
@AllArgsConstructor
public class UserController {
  private final UserRepository userRepository;
  private final PrototypeRepository prototypeRepository;
  private final LikesRepository likesRepository;
  private final DirectMessageRepository directMessageRepository;

  private final UserService userService;
  private final HttpServletRequest request;
  private final ImageUrl imageUrl;

  @ModelAttribute("user")
  public UserEntity addUserToModel(@AuthenticationPrincipal CustomUserDetail currentUser) {
    if (currentUser != null) {
      return userRepository.findById(currentUser.getUser().getId());
    }
    return null;
  }

  @GetMapping("/users/sign_up")
  public String showSignUp(Model model){
    model.addAttribute("userForm", new UserForm());
    return "users/signUp";
  }

  @PostMapping("/user")
  public String createUser(@ModelAttribute("userForm") @Validated(ValidationOrder.class) UserForm userForm, BindingResult result, Model model) {
    userForm.validatePasswordConfirmation(result);

    if (userRepository.existsByEmail(userForm.getEmail())) {
      result.rejectValue("email", "null", "そのメールアドレスは既に登録されています");
    }

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());

      model.addAttribute("errorMessages", errorMessages);
      model.addAttribute("userForm", userForm);
      return "users/signUp";
    }

    UserEntity userEntity = new UserEntity();
    userEntity.setNickname(userForm.getNickname());
    userEntity.setEmail(userForm.getEmail());
    userEntity.setPassword(userForm.getPassword());
    userEntity.setAffiliation(userForm.getAffiliation());
    userEntity.setProfile(userForm.getProfile());
    userEntity.setPosition(userForm.getPosition());

    MultipartFile avatarFile = userForm.getAvatar();
    String avatarPath = null;
    if (avatarFile != null && !avatarFile.isEmpty()) {
        try {
            String uploadDir = imageUrl.getUserAvatarUrl();
            Path uploadDirPath = Paths.get(uploadDir);

            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }
            String fileName = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + avatarFile.getOriginalFilename();
            Path imagePath = uploadDirPath.resolve(fileName);
            Files.copy(avatarFile.getInputStream(), imagePath);
            avatarPath = "/user_avatars/" + fileName; 
        } catch (IOException e) {
            result.rejectValue("avatar", "upload", "アイコン画像の保存に失敗しました");
            List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
            model.addAttribute("errorMessages", errorMessages);
            model.addAttribute("userForm", userForm);
            return "users/signUp";
        }
    } else {
        avatarPath = "/user_avatars/default_avatar.png";
    }
    userEntity.setAvatar(avatarPath);

    try {
      userService.createUserWithEncryptedPassword(userEntity);

      // 自動ログイン
      UserEntity savedUser = userRepository.findByEmail(userForm.getEmail());
    if (savedUser == null) {
        // エラー処理: 挿入直後に取れないのは異常
        model.addAttribute("errorMessages", List.of("ユーザー情報の取得に失敗しました。"));
        model.addAttribute("userForm", userForm);
        return "users/signUp";
    }

    // 登録後、自動ログイン
    CustomUserDetail userDetails = new CustomUserDetail(savedUser);

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            userDetails,
            userDetails.getPassword(),
            userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authToken);

    request.getSession().setAttribute(
      HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
      SecurityContextHolder.getContext()
    );
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "users/signUp";
    }

    return "redirect:/";
  }

  @GetMapping("/login")
  public String showLoginWithError(@RequestParam(value = "error") String error, Model model) {
    if (error != null) {
      model.addAttribute("loginError", "メールアドレス か パスワード が間違っています。");
    }
    return "users/login";
  }

  @GetMapping("/users/login")
  public String showLogin(){
      return "users/login";
  }

  @GetMapping("/users/{userId}")
  public String showMypage(
    @PathVariable("userId") Integer userId, 
    @AuthenticationPrincipal CustomUserDetail currentUser,
    @RequestParam(value = "tab", defaultValue = "own") String tab,
    Model model) {
    UserEntity user = userRepository.findById(userId);
    boolean isOwnPage = (currentUser != null && currentUser.getUser() != null
        && user.getId().equals(currentUser.getUser().getId()));
    model.addAttribute("isOwnPage", isOwnPage);

    List<PrototypeEntity> prototypes = prototypeRepository.findByUserId(userId);

    // いいね欄（タブ切り替え用）
    List<PrototypeEntity> likedPrototypes = List.of();
    List<Integer> likedIds = List.of();
    if (currentUser != null && user.getId().equals(currentUser.getUser().getId())) {
        likedIds = likesRepository.findLikedPrototypeIdsByUserId(userId);
        likedPrototypes = likedIds.stream()
            .map(prototypeRepository::findById)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        // DM履歴一覧
        List<DirectMessageEntity> latestDmList = directMessageRepository.findLatestMessagesByPartner(userId);
        model.addAttribute("latestDmList", latestDmList);
    }

    model.addAttribute("thisUser", user);
    model.addAttribute("prototypes", prototypes);
    model.addAttribute("likedPrototypes", likedPrototypes);
    model.addAttribute("likedIds", likedIds);
    model.addAttribute("tab", tab);
    return "users/detail";
  }

  @GetMapping("/users/{userId}/edit")
  public String showEditUser(@PathVariable("userId") Integer userId, Model model) {
      UserEntity user = userRepository.findById(userId);

      UserForm userForm = new UserForm();

      userForm.setNickname(user.getNickname());
      userForm.setEmail(user.getEmail());
      userForm.setProfile(user.getProfile());
      userForm.setAffiliation(user.getAffiliation());
      userForm.setPosition(user.getPosition());
      userForm.setPassword(user.getPassword());
      userForm.setPasswordConfirmation(user.getPassword());

      model.addAttribute("user", user);
      model.addAttribute("userForm", userForm);
      model.addAttribute("userId", userId);
    
    return "users/edit";
  }

  @PostMapping("/users/{userId}/update")
  public String editUser(@ModelAttribute("userForm") @Validated(ValidationOrder.class) UserForm userForm,
  BindingResult result,
  @AuthenticationPrincipal CustomUserDetail currentUser,
  @PathVariable("userId") Integer userId,
  Model model
  ) {

    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);

      model.addAttribute("userForm", userForm);
      model.addAttribute("userId", userId);
      return "users/edit";
    }

    UserEntity user = userRepository.findById(userId);
    user.setNickname(userForm.getNickname());
    user.setEmail(userForm.getEmail());
    user.setProfile(userForm.getProfile());
    user.setAffiliation(userForm.getAffiliation());
    user.setPosition(userForm.getPosition());
    user.setPassword(userForm.getPassword());

    MultipartFile avatarFile = userForm.getAvatar();
    String avatarPath = null;
    if (avatarFile != null && !avatarFile.isEmpty()) {
        try {
            String uploadDir = imageUrl.getUserAvatarUrl();
            Path uploadDirPath = Paths.get(uploadDir);

            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }
            String fileName = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + avatarFile.getOriginalFilename();
            Path imagePath = uploadDirPath.resolve(fileName);
            Files.copy(avatarFile.getInputStream(), imagePath);
            avatarPath = "/user_avatars/" + fileName; 
        } catch (IOException e) {
            result.rejectValue("avatar", "upload", "アイコン画像の保存に失敗しました");
            List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
            model.addAttribute("errorMessages", errorMessages);
            model.addAttribute("userForm", userForm);
            model.addAttribute("userId", userId);
            return "users/edit";
        }
    } else {
        avatarPath = user.getAvatar();
    }
    user.setAvatar(avatarPath);

      try {
      userService.updateUserWithEncryptedPassword(user);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "users/edit";
    }
      
      return "redirect:/users/" + userId;
  }

  @GetMapping("/users/{userId}/icon-edit")
  public String showUserIconEdit(
    @PathVariable("userId") Integer userId, 
    Model model,
    @AuthenticationPrincipal CustomUserDetail currentUser) {
      UserEntity user = userRepository.findById(userId);

      UserIconForm userIconForm = new UserIconForm();

      model.addAttribute("user", user);
      model.addAttribute("userIconForm", userIconForm);
      model.addAttribute("userId", userId);
    return "users/icon_edit";
  }

  @PostMapping("/users/{userId}/icon-update")
  public String updateUserIcon(
    @PathVariable("userId") Integer userId,
    @ModelAttribute("userIconForm") UserIconForm userIconForm,
    BindingResult result,
    @AuthenticationPrincipal CustomUserDetail currentUser,
    Model model
  ) {
    if (result.hasErrors()) {
      List<String> errorMessages = result.getAllErrors().stream()
              .map(DefaultMessageSourceResolvable::getDefaultMessage)
              .collect(Collectors.toList());
      model.addAttribute("errorMessages", errorMessages);

      model.addAttribute("userIconForm", userIconForm);
      model.addAttribute("userId", userId);
      return "users/icon_edit";
    }

    UserEntity user = userRepository.findById(userId);
    
    MultipartFile avatarFile = userIconForm.getAvatar();
    String avatarPath = null;
    if (avatarFile != null && !avatarFile.isEmpty()) {
        try {
            String uploadDir = imageUrl.getUserAvatarUrl();
            Path uploadDirPath = Paths.get(uploadDir);

            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }
            String fileName = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + avatarFile.getOriginalFilename();
            Path imagePath = uploadDirPath.resolve(fileName);
            Files.copy(avatarFile.getInputStream(), imagePath);
            avatarPath = "/user_avatars/" + fileName; 
        } catch (IOException e) {
            result.rejectValue("avatar", "upload", "アイコン画像の保存に失敗しました");
            List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
            model.addAttribute("errorMessages", errorMessages);
            model.addAttribute("userIconForm", userIconForm);
            model.addAttribute("userId", userId);
            return "users/icon_edit";
        }
    } else {
        avatarPath = user.getAvatar();
    }
    user.setAvatar(avatarPath);

    try {
      userRepository.updateIcon(userId, avatarPath);
  } catch (Exception e) {
      model.addAttribute("errorMessage", "保存時にエラーが発生しました：" + e.getMessage());
      model.addAttribute("user", user);
      model.addAttribute("userIconForm", userIconForm);
      model.addAttribute("userId", userId);
      return "users/icon_edit";
  }

  // 完了後はマイページへリダイレクト
  return "redirect:/users/" + userId;
  }
}
