package com.example.student.controller;

import com.example.student.entity.ClassInfo;
import com.example.student.service.ClassService;
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
@RequestMapping("/class")
public class ClassController {

    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String className,
                       @RequestParam(required = false) String major,
                       @RequestParam(defaultValue = "1") Integer page,
                       Model model,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"STUDENT".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        String keyword = className == null ? "" : className.trim();
        String selectedMajor = major == null ? "" : major.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<ClassInfo> classPage = classService.findAdminPage(
                keyword,
                selectedMajor,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (classPage.getTotalPages() > 0 && pageNumber > classPage.getTotalPages()) {
            pageNumber = classPage.getTotalPages();
            classPage = classService.findAdminPage(
                    keyword,
                    selectedMajor,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("classes", classPage.getContent());
        model.addAttribute("classPage", classPage);
        model.addAttribute("className", keyword);
        model.addAttribute("major", selectedMajor);
        model.addAttribute("majors", classService.findDistinctMajors());
        model.addAttribute("role", role);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", classPage.getTotalPages());
        model.addAttribute("totalElements", classPage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= classPage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "class/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/class";
        }
        return "class/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ClassInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/class";
        }
        Long id = formData.getId();
        ClassInfo classInfo;
        if (id != null) {
            classInfo = classService.findById(id).orElse(null);
            if (classInfo == null) {
                redirectAttributes.addFlashAttribute("error", "\u73ed\u7ea7\u4e0d\u5b58\u5728");
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
        redirectAttributes.addFlashAttribute("message", "\u4fdd\u5b58\u6210\u529f");
        return "redirect:/class";
    }


    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(required = false) String className,
                              @RequestParam(required = false) String major,
                              @RequestParam(required = false) Integer page,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/class";
        }
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
        if (className != null) redirectAttributes.addAttribute("className", className);
        if (major != null) redirectAttributes.addAttribute("major", major);
        if (page != null) redirectAttributes.addAttribute("page", page);
            return "redirect:/class";
        }
        try {
            int count = classService.batchDeleteByIds(ids);
            if (count == 0) {
                redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
            } else {
                redirectAttributes.addFlashAttribute("message", "\u6210\u529f\u5220\u9664 " + count + " \u6761\u8bb0\u5f55");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u6279\u91cf\u5220\u9664\u5931\u8d25\uff0c\u90e8\u5206\u8bb0\u5f55\u53ef\u80fd\u5b58\u5728\u5173\u8054\u6570\u636e");
        }
        if (className != null) redirectAttributes.addAttribute("className", className);
        if (major != null) redirectAttributes.addAttribute("major", major);
        if (page != null) redirectAttributes.addAttribute("page", page);
        return "redirect:/class";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/class";
        }
        classService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "\u5220\u9664\u6210\u529f");
        return "redirect:/class";
    }
}
