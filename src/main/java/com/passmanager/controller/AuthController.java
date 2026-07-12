package com.passmanager.controller;

import com.passmanager.dto.RegisterDto;
import com.passmanager.entity.SecurityQuestion;
import com.passmanager.entity.User;
import com.passmanager.security.TOTPUtil;
import com.passmanager.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
// Note: SecurityQuestion and List are still used by findAccount() which shows the security-questions view.

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final TOTPUtil totpUtil;

    public AuthController(UserService userService, TOTPUtil totpUtil) {
        this.userService = userService;
        this.totpUtil = totpUtil;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout, Model model) {
        if (error != null)
            model.addAttribute("error", "Invalid username or password");
        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDto registerDto, Model model, RedirectAttributes ra) {
        if (registerDto.getMasterPassword() == null || registerDto.getMasterPassword().length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters");
            model.addAttribute("registerDto", registerDto);
            return "auth/register";
        }
        if (!registerDto.getMasterPassword().equals(registerDto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("registerDto", registerDto);
            return "auth/register";
        }
        if (registerDto.getQuestion1() == null || registerDto.getAnswer1() == null || registerDto.getAnswer1().trim().isEmpty() ||
                registerDto.getQuestion2() == null || registerDto.getAnswer2() == null || registerDto.getAnswer2().trim().isEmpty()) {
            model.addAttribute("error", "Please provide both security questions and answers");
            model.addAttribute("registerDto", registerDto);
            return "auth/register";
        }
        try {
            userService.register(registerDto);
            ra.addFlashAttribute("success", "Account created! Please login.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDto", registerDto);
            return "auth/register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password/find")
    public String findAccount(@RequestParam String email, Model model, HttpSession session) {
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that email");
            return "auth/forgot-password";
        }
        User user = userOpt.get();
        List<SecurityQuestion> questions = userService.getSecurityQuestions(user);
        if (questions.isEmpty()) {
            model.addAttribute("error", "No security questions configured for this account");
            return "auth/forgot-password";
        }
        session.setAttribute("resetUserId", user.getId());
        model.addAttribute("questions", questions);
        model.addAttribute("userId", user.getId());
        return "auth/security-questions";
    }

    // NOTE: The full forgot-password flow (security questions + OTP) is handled
    // by ForgotPasswordController at /forgot-password/**.
    // The stubs below have been intentionally removed to avoid a broken duplicate
    // flow that skipped actual answer verification and never called resetPassword().

    @GetMapping("/verify-2fa")
    public String verify2FAPage(HttpSession session, Model model) {
        if (session.getAttribute("TWO_FACTOR_CHECK_PENDING") == null) {
            return "redirect:/auth/login";
        }
        return "auth/verify-2fa";
    }

    @PostMapping("/verify-2fa")
    public String verify2FA(@RequestParam String code, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("TWO_FACTOR_USER_ID");
        if (userId == null)
            return "redirect:/auth/login";

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (totpUtil.verifyCode(user.getTwoFactorSecret(), code)) {
                // Remove the pending flag and redirect to dashboard
                session.removeAttribute("TWO_FACTOR_CHECK_PENDING");
                return "redirect:/dashboard";
            } else {
                model.addAttribute("error", "Invalid 2FA code. Please try again.");
                return "auth/verify-2fa";
            }
        }
        return "redirect:/auth/login";
    }
}
