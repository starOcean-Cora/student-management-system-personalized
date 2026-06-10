package com.example.student.controller;

import com.example.student.entity.ClassInfo;
import com.example.student.service.ClassService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/class")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("classes", classService.findAll());
        model.addAttribute("role", session.getAttribute("role"));
        return "class/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/class";
        }
        return "class/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ClassInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/class";
        }
        Long id = formData.getId();
        ClassInfo classInfo;
        if (id != null) {
            classInfo = classService.findById(id).orElse(null);
            if (classInfo == null) {
                redirectAttributes.addFlashAttribute("error", "班级不存在");
                return "redirect:/class";
            }
        } else {
            classInfo = new ClassInfo();
        }
        classInfo.setClassName(formData.getClassName());
        classInfo.setGrade(formData.getGrade());
        classInfo.setMajor(formData.getMajor());
        classInfo.setTeacherName(formData.getTeacherName());
        classInfo.setRemark(formData.getRemark());
        classService.save(classInfo);
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/class";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/class";
        }
        classService.findById(id).ifPresent(c -> model.addAttribute("classInfo", c));
        return "class/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/class";
        }
        classService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/class";
    }
}