package com.example.student.controller;

import com.example.student.entity.LeaveInfo;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.LeaveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/leave")
public class LeaveController {

    private final LeaveService leaveService;
    private final StudentInfoRepository studentInfoRepository;

    public LeaveController(LeaveService leaveService,
                           StudentInfoRepository studentInfoRepository) {
        this.leaveService = leaveService;
        this.studentInfoRepository = studentInfoRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        model.addAttribute("leaves", leaveService.findAll());
        model.addAttribute("role", session.getAttribute("role"));
        return "leave/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        return "leave/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute LeaveInfo formData,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        Long id = formData.getId();
        if (id != null) {
            LeaveInfo leave = leaveService.findById(id).orElse(null);
            if (leave == null) {
                redirectAttributes.addFlashAttribute("error", "请假记录不存在");
                return "redirect:/leave";
            }
            leave.setLeaveType(formData.getLeaveType());
            leave.setReason(formData.getReason());
            leave.setStartTime(formData.getStartTime());
            leave.setEndTime(formData.getEndTime());
            leave.setStatus(formData.getStatus());
            leave.setRemark(formData.getRemark());
            leaveService.save(leave);
        } else {
            LeaveInfo leave = new LeaveInfo();
            leave.setStudentId(formData.getStudentId());
            leave.setLeaveType(formData.getLeaveType());
            leave.setReason(formData.getReason());
            leave.setStartTime(formData.getStartTime());
            leave.setEndTime(formData.getEndTime());
            leave.setStatus(formData.getStatus() != null ? formData.getStatus() : "待审批");
            leave.setRemark(formData.getRemark());
            leaveService.fillRedundantFields(leave);
            leaveService.save(leave);
        }
        redirectAttributes.addFlashAttribute("message", "保存成功");
        return "redirect:/leave";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        LeaveInfo leave = leaveService.findById(id).orElse(null);
        if (leave != null) {
            model.addAttribute("leaveInfo", leave);
        }
        model.addAttribute("students", studentInfoRepository.findAll());
        return "leave/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        leaveService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "删除成功");
        return "redirect:/leave";
    }

    @GetMapping("/approve/{id}")
    public String approveForm(@PathVariable Long id,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        LeaveInfo leave = leaveService.findById(id).orElse(null);
        if (leave != null) {
            model.addAttribute("leaveInfo", leave);
        }
        return "leave/approve";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam Long id,
                          @RequestParam String status,
                          @RequestParam(required = false) String approveRemark,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/leave";
        }
        LeaveInfo leave = leaveService.findById(id).orElse(null);
        if (leave == null) {
            redirectAttributes.addFlashAttribute("error", "请假记录不存在");
            return "redirect:/leave";
        }
        Long adminId = (Long) session.getAttribute("userId");
        leave.setApproverId(adminId);
        leave.setApproveTime(LocalDateTime.now());
        leave.setApproveRemark(approveRemark);
        leave.setStatus(status);
        leaveService.save(leave);
        redirectAttributes.addFlashAttribute("message", "审批成功");
        return "redirect:/leave";
    }

    @GetMapping("/my")
    public String myLeaves(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        if (studentId == null) {
            redirectAttributes.addFlashAttribute("error", "未关联学生信息");
            return "redirect:/";
        }
        model.addAttribute("leaves", leaveService.findByStudentId(studentId));
        return "leave/my";
    }

    @GetMapping("/apply")
    public String applyForm(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "没有权限执行此操作");
            return "redirect:/";
        }
        return "leave/apply";
    }

    @PostMapping("/apply")
    public String apply(@ModelAttribute LeaveInfo formData,
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
        LeaveInfo leave = new LeaveInfo();
        leave.setStudentId(studentId);
        leave.setLeaveType(formData.getLeaveType());
        leave.setReason(formData.getReason());
        leave.setStartTime(formData.getStartTime());
        leave.setEndTime(formData.getEndTime());
        leave.setStatus("待审批");
        leave.setRemark(formData.getRemark());
        leaveService.fillRedundantFields(leave);
        leaveService.save(leave);
        redirectAttributes.addFlashAttribute("message", "请假申请已提交");
        return "redirect:/leave/my";
    }
}