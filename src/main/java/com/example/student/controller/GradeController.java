package com.example.student.controller;

import com.example.student.entity.GradeInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.ClassInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.GradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String list(@RequestParam(required = false) String courseName,
                       @RequestParam(required = false) String examType,
                       @RequestParam(defaultValue = "1") Integer page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        String courseKeyword = courseName == null ? "" : courseName.trim();
        String selectedExamType = examType == null ? "" : examType.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<GradeInfo> gradePage = gradeService.findAdminPage(
                courseKeyword,
                selectedExamType,
                PageRequest.of(pageNumber - 1, 10)
        );
        if (gradePage.getTotalPages() > 0 && pageNumber > gradePage.getTotalPages()) {
            pageNumber = gradePage.getTotalPages();
            gradePage = gradeService.findAdminPage(
                    courseKeyword,
                    selectedExamType,
                    PageRequest.of(pageNumber - 1, 10)
            );
        }
        model.addAttribute("grades", gradePage.getContent());
        model.addAttribute("gradePage", gradePage);
        model.addAttribute("courseName", courseKeyword);
        model.addAttribute("examType", selectedExamType);
        model.addAttribute("examTypes", Arrays.asList("期中考试", "期末考试", "平时成绩"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", gradePage.getTotalPages());
        model.addAttribute("totalElements", gradePage.getTotalElements());
        model.addAttribute("rankOffset", (pageNumber - 1) * 10);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= gradePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
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


    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(required = false) String courseName,
                              @RequestParam(required = false) String examType,
                              @RequestParam(required = false) Integer page,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/grade";
        }
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
        if (courseName != null) redirectAttributes.addAttribute("courseName", courseName);
        if (examType != null) redirectAttributes.addAttribute("examType", examType);
        if (page != null) redirectAttributes.addAttribute("page", page);
            return "redirect:/grade";
        }
        try {
            int count = gradeService.batchDeleteByIds(ids);
            if (count == 0) {
                redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
            } else {
                redirectAttributes.addFlashAttribute("message", "\u6210\u529f\u5220\u9664 " + count + " \u6761\u8bb0\u5f55");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u6279\u91cf\u5220\u9664\u5931\u8d25\uff0c\u90e8\u5206\u8bb0\u5f55\u53ef\u80fd\u5b58\u5728\u5173\u8054\u6570\u636e");
        }
        if (courseName != null) redirectAttributes.addAttribute("courseName", courseName);
        if (examType != null) redirectAttributes.addAttribute("examType", examType);
        if (page != null) redirectAttributes.addAttribute("page", page);
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
    public String myGrades(@RequestParam(required = false) String courseName,
                           @RequestParam(required = false) String examType,
                           @RequestParam(defaultValue = "1") Integer page,
                           HttpSession session,
                           Model model,
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
        String courseKeyword = courseName == null ? "" : courseName.trim();
        String selectedExamType = examType == null ? "" : examType.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<GradeInfo> gradePage = gradeService.findStudentPage(
                studentId,
                courseKeyword,
                selectedExamType,
                PageRequest.of(pageNumber - 1, 10)
        );
        if (gradePage.getTotalPages() > 0 && pageNumber > gradePage.getTotalPages()) {
            pageNumber = gradePage.getTotalPages();
            gradePage = gradeService.findStudentPage(
                    studentId,
                    courseKeyword,
                    selectedExamType,
                    PageRequest.of(pageNumber - 1, 10)
            );
        }
        model.addAttribute("grades", gradePage.getContent());
        model.addAttribute("gradePage", gradePage);
        model.addAttribute("courseName", courseKeyword);
        model.addAttribute("examType", selectedExamType);
        model.addAttribute("examTypes", Arrays.asList("期中考试", "期末考试", "平时成绩"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", gradePage.getTotalPages());
        model.addAttribute("totalElements", gradePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= gradePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
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
