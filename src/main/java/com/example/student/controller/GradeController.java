package com.example.student.controller;

import com.example.student.entity.GradeInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.ClassInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.GradeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/grade")
public class GradeController {

    private final GradeService gradeService;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;
    private final ClassInfoRepository classInfoRepository;

    public GradeController(GradeService gradeService,
                           StudentInfoRepository studentInfoRepository,
                           CourseInfoRepository courseInfoRepository,
                           ClassInfoRepository classInfoRepository) {
        this.gradeService = gradeService;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
        this.classInfoRepository = classInfoRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        model.addAttribute("grades", gradeService.findAllOrderByScoreDesc());
        model.addAttribute("role", session.getAttribute("role"));
        return "grade/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/grade";
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        model.addAttribute("courses", courseInfoRepository.findAll());
        return "grade/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute GradeInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/grade";
        }
        Long id = formData.getId();
        if (id != null) {
            GradeInfo grade = gradeService.findById(id).orElse(null);
            if (grade == null) {
                redirectAttributes.addFlashAttribute("error", "成绩记录不存在");
                return "redirect:/grade";
            }
            Double score = formData.getScore();
            if (score == null || score < 0 || score > 100) {
                redirectAttributes.addFlashAttribute("error", "成绩必须在0到100之间");
                return "redirect:/grade/edit/" + id;
            }
            grade.setScore(score);
            grade.setRemark(formData.getRemark());
            gradeService.save(grade);
        } else {
            Double score = formData.getScore();
            if (score == null || score < 0 || score > 100) {
                redirectAttributes.addFlashAttribute("error", "成绩必须在0到100之间");
                return "redirect:/grade/add";
            }
            Long studentId = formData.getStudentId();
            Long courseId = formData.getCourseId();
            String examType = formData.getExamType();
            if (gradeService.isDuplicate(studentId, courseId, examType)) {
                redirectAttributes.addFlashAttribute("error", "该学生在此课程的同类型成绩已存在，不能重复录入");
                return "redirect:/grade/add";
            }
            GradeInfo grade = new GradeInfo();
            grade.setStudentId(studentId);
            grade.setCourseId(courseId);
            grade.setExamType(examType);
            grade.setScore(score);
            grade.setRemark(formData.getRemark());
            gradeService.fillRedundantFields(grade);
            gradeService.save(grade);
        }
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/grade";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/grade";
        }
        GradeInfo grade = gradeService.findById(id).orElse(null);
        if (grade != null) {
            model.addAttribute("gradeInfo", grade);
        }
        return "grade/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/grade";
        }
        gradeService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/grade";
    }

    @GetMapping("/my")
    public String myGrades(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }
        model.addAttribute("grades", gradeService.findByStudentId(studentId));
        return "grade/my";
    }
    @GetMapping("/statistics")
    public String statistics(@RequestParam(required = false) Long courseId,
                             @RequestParam(required = false) Long classId,
                             @RequestParam(required = false) String examType,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        model.addAttribute("courses", courseInfoRepository.findAll());
        model.addAttribute("classes", classInfoRepository.findAll());
        model.addAttribute("courseId", courseId);
        model.addAttribute("classId", classId);
        model.addAttribute("examType", examType);
        model.addAttribute("stats", gradeService.getStatistics(courseId, classId, examType));
        return "grade/statistics";
    }
}