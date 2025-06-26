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
import in.tech_camp.protospace_c.entity.PrototypeEntity;
import in.tech_camp.protospace_c.entity.UserEntity;
import in.tech_camp.protospace_c.form.UserForm;
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

  private final UserService userService;
  private final HttpServletRequest request;
  private final ImageUrl imageUrl;

  @ModelAttribute("user")
  public UserEntity addUserToModel(@AuthenticationPrincipal CustomUserDetail currentUser) {
    if (currentUser != null) {
      return currentUser.getUser();
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
      result.rejectValue("email", "null", "Email already exists");
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
            // 路径获取和文件名处理
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
      model.addAttribute("loginError", "Invalid email or password.");
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
    Model model) {
    UserEntity user = userRepository.findById(userId);
    List<PrototypeEntity> prototypes = prototypeRepository.findByUserId(userId);

    model.addAttribute("user", user);
    model.addAttribute("prototypes", prototypes);

    List<Integer> likedIds = likesRepository.findLikedPrototypeIdsByUserId(currentUser.getUser().getId());
    model.addAttribute("likedIds", likedIds);
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

      try {
      userService.updateUserWithEncryptedPassword(user);
    } catch (Exception e) {
      System.out.println("エラー：" + e);
      return "users/edit";
    }
      
      return "redirect:/users/" + userId;
  }
  
  
}
