package com.example.student.controller;

import com.example.student.entity.CourseInfo;
import com.example.student.entity.CourseSelection;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.CourseSelectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/selection")
public class CourseSelectionController {

    private final CourseSelectionService selectionService;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;

    public CourseSelectionController(CourseSelectionService selectionService,
                                     StudentInfoRepository studentInfoRepository,
                                     CourseInfoRepository courseInfoRepository) {
        this.selectionService = selectionService;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        model.addAttribute("selections", selectionService.findAll());
        return "selection/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/selection";
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        model.addAttribute("courses", courseInfoRepository.findAll());
        return "selection/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute CourseSelection selection,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/selection";
        }
        Long id = selection.getId();
        if (id != null) {
            CourseSelection existing = selectionService.findById(id).orElse(null);
            if (existing != null) {
                existing.setStatus(selection.getStatus());
                selectionService.save(existing);
            }
        } else {
            if (selectionService.isAlreadySelected(selection.getStudentId(), selection.getCourseId())) {
                redirectAttributes.addFlashAttribute("error", "该学生已选择此课程");
                return "redirect:/selection/add";
            }
            selectionService.selectCourse(selection.getStudentId(), selection.getCourseId());
        }
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/selection";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/selection";
        }
        selectionService.findById(id).ifPresent(s -> model.addAttribute("selection", s));
        return "selection/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/selection";
        }
        selectionService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/selection";
    }

    @GetMapping("/my")
    public String mySelections(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }

        List<CourseSelection> mySelections = selectionService.findByStudentId(studentId);
        List<CourseInfo> allCourses = courseInfoRepository.findAll();

        Set<Long> selectedCourseIds = mySelections.stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toSet());

        List<CourseInfo> availableCourses = new ArrayList<>();
        for (CourseInfo c : allCourses) {
            if (!selectedCourseIds.contains(c.getId())) {
                availableCourses.add(c);
            }
        }

        model.addAttribute("mySelections", mySelections);
        model.addAttribute("availableCourses", availableCourses);
        return "selection/my";
    }

    @GetMapping("/select/{courseId}")
    public String selectCourse(@PathVariable Long courseId,
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
        if (selectionService.isAlreadySelected(studentId, courseId)) {
            redirectAttributes.addFlashAttribute("error", "不能重复选课");
            return "redirect:/selection/my";
        }
        selectionService.selectCourse(studentId, courseId);
        redirectAttributes.addFlashAttribute("message", "选课成功");
        return "redirect:/selection/my";
    }

    @GetMapping("/cancel/{id}")
    public String cancelSelection(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        CourseSelection selection = selectionService.findById(id).orElse(null);
        if (selection == null || !selection.getStudentId().equals(studentId)) {
            redirectAttributes.addFlashAttribute("error", "没有权限取消此选课");
            return "redirect:/selection/my";
        }
        selectionService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "已取消选课");
        return "redirect:/selection/my";
    }

    @GetMapping("/repair")
    public String repair(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/selection";
        }
        int count = selectionService.repairAllNullClasses();
        redirectAttributes.addFlashAttribute("message", "已修复 " + count + " 条选课记录的班级信息");
        return "redirect:/selection";
    }}