package com.example.student.controller;

import com.example.student.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/system")
public class SystemController {

    private final UserService userService;

    public SystemController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/password")
    public String passwordForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("userId") == null) {
            redirectAttributes.addFlashAttribute("error", "请先登录");
            return "redirect:/login";
        }
        return "system/password";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String oldPassword,
                                  @RequestParam String newPassword,
                                  @RequestParam String confirmPassword,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "请先登录");
            return "redirect:/login";
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            model.addAttribute("error", "新密码不能为空");
            return "system/password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "两次密码输入不一致");
            return "system/password";
        }
        boolean ok = userService.changePassword(userId, oldPassword, newPassword);
        if (!ok) {
            model.addAttribute("error", "原密码错误");
            return "system/password";
        }
        redirectAttributes.addFlashAttribute("message", "密码修改成功，请重新登录");
        return "redirect:/logout";
    }
}