package com.example.student.controller;

import com.example.student.entity.StudentInfo;
import com.example.student.service.ClassService;
import com.example.student.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final ClassService classService;

    public StudentController(StudentService studentService,
                             ClassService classService) {
        this.studentService = studentService;
        this.classService = classService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) Long classId,
                       @RequestParam(defaultValue = "1") Integer page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if ("STUDENT".equals(role)) {
            return "redirect:/student/profile";
        }
        if (!"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        String keyword = name == null ? "" : name.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<StudentInfo> studentPage = studentService.findAdminPage(
                keyword,
                classId,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (studentPage.getTotalPages() > 0 && pageNumber > studentPage.getTotalPages()) {
            pageNumber = studentPage.getTotalPages();
            studentPage = studentService.findAdminPage(
                    keyword,
                    classId,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("studentPage", studentPage);
        model.addAttribute("name", keyword);
        model.addAttribute("classId", classId);
        model.addAttribute("classes", classService.findAll());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalElements", studentPage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= studentPage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
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
    public String save(@ModelAttribute StudentInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/student";
        }
        Long id = formData.getId();
        boolean isEdit = (id != null && id > 0);
        // Load original record for edits before duplicate check
        StudentInfo student;
        if (isEdit) {
            student = studentService.findById(id).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "学生不存在");
                return "redirect:/student";
            }
            // Only check duplicate if studentNo actually changed
            if (!student.getStudentNo().equals(formData.getStudentNo())) {
                if (studentService.isStudentNoDuplicate(formData.getStudentNo(), student.getId())) {
                    redirectAttributes.addFlashAttribute("error", "学号已存在");
                    redirectAttributes.addFlashAttribute("studentInfo", formData);
                    return "redirect:/student/edit/" + id;
                }
            }
        } else {
            if (studentService.isStudentNoDuplicate(formData.getStudentNo(), null)) {
                redirectAttributes.addFlashAttribute("error", "学号已存在");
                redirectAttributes.addFlashAttribute("studentInfo", formData);
                return "redirect:/student/add";
            }
            student = new StudentInfo();
        }
        student.setStudentNo(formData.getStudentNo());
        student.setName(formData.getName());
        student.setGender(formData.getGender());
        student.setClassId(formData.getClassId());
        student.setClassName(formData.getClassName());
        student.setPhone(formData.getPhone());
        student.setEmail(formData.getEmail());
        student.setAvatar(formData.getAvatar());
        student.setStatus(formData.getStatus());
        studentService.save(student);
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
