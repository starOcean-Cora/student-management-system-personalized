package com.example.student.controller;

import com.example.student.entity.AttendanceInfo;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.AttendanceService;
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
    public String list(@RequestParam(required = false) String studentNo,
                       @RequestParam(required = false) Long studentId,
                       @RequestParam(defaultValue = "1") Integer page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        String keyword = studentNo == null ? "" : studentNo.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<AttendanceInfo> attendancePage = attendanceService.findAdminPage(
                keyword,
                studentId,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (attendancePage.getTotalPages() > 0 && pageNumber > attendancePage.getTotalPages()) {
            pageNumber = attendancePage.getTotalPages();
            attendancePage = attendanceService.findAdminPage(
                    keyword,
                    studentId,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("attendances", attendancePage.getContent());
        model.addAttribute("attendancePage", attendancePage);
        model.addAttribute("studentNo", keyword);
        model.addAttribute("studentId", studentId);
        model.addAttribute("students", studentInfoRepository.findAll());
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("totalElements", attendancePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= attendancePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "attendance/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/attendance";
        }
        Long id = formData.getId();
        if (id != null) {
            AttendanceInfo attendance = attendanceService.findById(id).orElse(null);
            if (attendance == null) {
                redirectAttributes.addFlashAttribute("error", "\u8003\u52e4\u8bb0\u5f55\u4e0d\u5b58\u5728");
                return "redirect:/attendance";
            }
            attendance.setAttendanceDate(formData.getAttendanceDate());
            attendance.setStatus(formData.getStatus());
            attendance.setRemark(formData.getRemark());
            attendanceService.save(attendance);
        } else {
            AttendanceInfo attendance = new AttendanceInfo();
            attendance.setStudentId(formData.getStudentId());
            attendance.setCourseId(formData.getCourseId());
            attendance.setAttendanceDate(formData.getAttendanceDate());
            attendance.setStatus(formData.getStatus());
            attendance.setRemark(formData.getRemark());
            attendanceService.fillRedundantFields(attendance);
            attendanceService.save(attendance);
        }
        redirectAttributes.addFlashAttribute("message", "\u4fdd\u5b58\u6210\u529f");
        return "redirect:/attendance";
    }


    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(required = false) String studentNo,
                              @RequestParam(required = false) Long studentId,
                              @RequestParam(required = false) Integer page,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/attendance";
        }
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
        if (studentNo != null) redirectAttributes.addAttribute("studentNo", studentNo);
        if (studentId != null) redirectAttributes.addAttribute("studentId", studentId);
        if (page != null) redirectAttributes.addAttribute("page", page);
            return "redirect:/attendance";
        }
        try {
            int count = attendanceService.batchDeleteByIds(ids);
            if (count == 0) {
                redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
            } else {
                redirectAttributes.addFlashAttribute("message", "\u6210\u529f\u5220\u9664 " + count + " \u6761\u8bb0\u5f55");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u6279\u91cf\u5220\u9664\u5931\u8d25\uff0c\u90e8\u5206\u8bb0\u5f55\u53ef\u80fd\u5b58\u5728\u5173\u8054\u6570\u636e");
        }
        if (studentNo != null) redirectAttributes.addAttribute("studentNo", studentNo);
        if (studentId != null) redirectAttributes.addAttribute("studentId", studentId);
        if (page != null) redirectAttributes.addAttribute("page", page);
        return "redirect:/attendance";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/attendance";
        }
        attendanceService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "\u5220\u9664\u6210\u529f");
        return "redirect:/attendance";
    }

    @GetMapping("/my")
    public String myAttendance(@RequestParam(required = false) String courseName,
                               @RequestParam(required = false) Long courseId,
                               @RequestParam(defaultValue = "1") Integer page,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "\u672a\u5173\u8054\u5b66\u751f\u4fe1\u606f");
            return "redirect:/";
        }
        String keyword = courseName == null ? "" : courseName.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<AttendanceInfo> attendancePage = attendanceService.findStudentPage(
                studentId,
                keyword,
                courseId,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (attendancePage.getTotalPages() > 0 && pageNumber > attendancePage.getTotalPages()) {
            pageNumber = attendancePage.getTotalPages();
            attendancePage = attendanceService.findStudentPage(
                    studentId,
                    keyword,
                    courseId,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("attendances", attendancePage.getContent());
        model.addAttribute("attendancePage", attendancePage);
        model.addAttribute("courses", courseInfoRepository.findAll());
        model.addAttribute("courseName", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("totalElements", attendancePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= attendancePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "attendance/my";
    }

}
