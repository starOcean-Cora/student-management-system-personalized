package com.example.student.controller;

import com.example.student.entity.StudentInfo;
import com.example.student.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if ("STUDENT".equals(role)) {
            return "redirect:/student/profile";
        }
        model.addAttribute("students", studentService.findAll());
        return "student/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/student";
        }
        return "student/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute StudentInfo studentInfo,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/student";
        }
        Long id = studentInfo.getId();
        boolean isEdit = (id != null);
        Long excludeId = isEdit ? id : 0L;
        if (studentService.isStudentNoDuplicate(studentInfo.getStudentNo(), excludeId)) {
            redirectAttributes.addFlashAttribute("error", "学号已存在");
            redirectAttributes.addFlashAttribute("studentInfo", studentInfo);
            return isEdit ? "redirect:/student/edit/" + id : "redirect:/student/add";
        }
        studentService.save(studentInfo);
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/student";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/student";
        }
        studentService.findById(id).ifPresent(s -> model.addAttribute("studentInfo", s));
        return "student/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/student";
        }
        studentService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/student";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }
        studentService.findById(studentId).ifPresent(s -> model.addAttribute("studentInfo", s));
        return "student/profile";
    }

    @PostMapping("/profile/avatar")
    public String updateAvatar(@RequestParam String avatar,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }
        StudentInfo student = studentService.findById(studentId).orElse(null);
        if (student != null) {
            student.setAvatar(avatar);
            studentService.save(student);
            redirectAttributes.addFlashAttribute("message", "头像更新成功");
        }
        return "redirect:/student/profile";
    }
}