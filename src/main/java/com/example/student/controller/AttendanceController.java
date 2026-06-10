package com.example.student.controller;

import com.example.student.entity.AttendanceInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.AttendanceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentInfoRepository studentInfoRepository;
    private final CourseInfoRepository courseInfoRepository;

    public AttendanceController(AttendanceService attendanceService,
                                StudentInfoRepository studentInfoRepository,
                                CourseInfoRepository courseInfoRepository) {
        this.attendanceService = attendanceService;
        this.studentInfoRepository = studentInfoRepository;
        this.courseInfoRepository = courseInfoRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        model.addAttribute("attendances", attendanceService.findAll());
        model.addAttribute("role", session.getAttribute("role"));
        return "attendance/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/attendance";
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        model.addAttribute("courses", courseInfoRepository.findAll());
        return "attendance/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute AttendanceInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/attendance";
        }
        Long id = formData.getId();
        AttendanceInfo attendance;
        if (id != null) {
            attendance = attendanceService.findById(id).orElse(null);
            if (attendance == null) {
                redirectAttributes.addFlashAttribute("error", "考勤记录不存在");
                return "redirect:/attendance";
            }
        } else {
            attendance = new AttendanceInfo();
        }
        attendance.setStudentId(formData.getStudentId());
        attendance.setCourseId(formData.getCourseId());
        attendance.setAttendanceDate(formData.getAttendanceDate());
        attendance.setStatus(formData.getStatus());
        attendance.setRemark(formData.getRemark());
        attendanceService.fillRedundantFields(attendance);
        attendanceService.save(attendance);
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/attendance";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/attendance";
        }
        AttendanceInfo attendance = attendanceService.findById(id).orElse(null);
        if (attendance != null) {
            model.addAttribute("attendance", attendance);
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        model.addAttribute("courses", courseInfoRepository.findAll());
        return "attendance/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/attendance";
        }
        attendanceService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/attendance";
    }

    @GetMapping("/my")
    public String myAttendance(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }
        model.addAttribute("attendances", attendanceService.findByStudentId(studentId));
        return "attendance/my";
    }
}