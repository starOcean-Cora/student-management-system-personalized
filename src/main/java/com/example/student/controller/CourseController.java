package com.example.student.controller;

import com.example.student.entity.CourseInfo;
import com.example.student.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute("role", session.getAttribute("role"));
        return "course/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/course";
        }
        return "course/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CourseInfo courseInfo,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/course";
        }
        Long id = courseInfo.getId();
        boolean isEdit = (id != null);
        Long excludeId = isEdit ? id : 0L;
        if (courseService.isCourseCodeDuplicate(courseInfo.getCourseCode(), excludeId)) {
            redirectAttributes.addFlashAttribute("error", "课程编号已存在");
            return "redirect:/course/form";
        }
        courseService.save(courseInfo);
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/course";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/course";
        }
        courseService.findById(id).ifPresent(c -> model.addAttribute("courseInfo", c));
        return "course/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/course";
        }
        courseService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/course";
    }
}