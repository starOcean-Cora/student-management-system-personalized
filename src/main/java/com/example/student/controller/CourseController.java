package com.example.student.controller;

import com.example.student.entity.CourseInfo;
import com.example.student.service.CourseService;
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
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String courseName,
                       @RequestParam(required = false) String courseCode,
                       @RequestParam(defaultValue = "1") Integer page,
                       Model model,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"STUDENT".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        String keyword = courseName == null ? "" : courseName.trim();
        String selectedCode = courseCode == null ? "" : courseCode.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<CourseInfo> coursePage = courseService.findListPage(
                keyword,
                selectedCode,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (coursePage.getTotalPages() > 0 && pageNumber > coursePage.getTotalPages()) {
            pageNumber = coursePage.getTotalPages();
            coursePage = courseService.findListPage(
                    keyword,
                    selectedCode,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("coursePage", coursePage);
        model.addAttribute("courseName", keyword);
        model.addAttribute("courseCode", selectedCode);
        model.addAttribute("courseCodes", courseService.findDistinctCourseCodes());
        model.addAttribute("role", role);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        model.addAttribute("totalElements", coursePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= coursePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "course/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/course";
        }
        return "course/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CourseInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/course";
        }
        Long id = formData.getId();
        boolean isEdit = (id != null);
        Long excludeId = isEdit ? id : 0L;
        if (courseService.isCourseCodeDuplicate(formData.getCourseCode(), excludeId)) {
            redirectAttributes.addFlashAttribute("error", "\u8bfe\u7a0b\u7f16\u53f7\u5df2\u5b58\u5728");
            return isEdit ? "redirect:/course/edit/" + id : "redirect:/course/add";
        }
        CourseInfo course;
        if (isEdit) {
            course = courseService.findById(id).orElse(null);
            if (course == null) {
                redirectAttributes.addFlashAttribute("error", "\u8bfe\u7a0b\u4e0d\u5b58\u5728");
                return "redirect:/course";
            }
        } else {
            course = new CourseInfo();
        }
        course.setCourseCode(formData.getCourseCode());
        course.setCourseName(formData.getCourseName());
        course.setTeacherName(formData.getTeacherName());
        course.setCredit(formData.getCredit());
        course.setClassHours(formData.getClassHours());
        course.setRemark(formData.getRemark());
        courseService.save(course);
        redirectAttributes.addFlashAttribute("message", "\u4fdd\u5b58\u6210\u529f");
        return "redirect:/course";
    }


    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(required = false) String courseName,
                              @RequestParam(required = false) String courseCode,
                              @RequestParam(required = false) Integer page,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/course";
        }
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
        if (courseName != null) redirectAttributes.addAttribute("courseName", courseName);
        if (courseCode != null) redirectAttributes.addAttribute("courseCode", courseCode);
        if (page != null) redirectAttributes.addAttribute("page", page);
            return "redirect:/course";
        }
        try {
            int count = courseService.batchDeleteByIds(ids);
            if (count == 0) {
                redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
            } else {
                redirectAttributes.addFlashAttribute("message", "\u6210\u529f\u5220\u9664 " + count + " \u6761\u8bb0\u5f55");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u6279\u91cf\u5220\u9664\u5931\u8d25\uff0c\u90e8\u5206\u8bb0\u5f55\u53ef\u80fd\u5b58\u5728\u5173\u8054\u6570\u636e");
        }
        if (courseName != null) redirectAttributes.addAttribute("courseName", courseName);
        if (courseCode != null) redirectAttributes.addAttribute("courseCode", courseCode);
        if (page != null) redirectAttributes.addAttribute("page", page);
        return "redirect:/course";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/course";
        }
        courseService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "\u5220\u9664\u6210\u529f");
        return "redirect:/course";
    }
}
