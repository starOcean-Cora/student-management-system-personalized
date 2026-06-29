package com.example.student.controller;

import com.example.student.entity.LeaveInfo;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.LeaveService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public String list(@RequestParam(required = false) String leaveType,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "1") Integer page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        String typeKeyword = leaveType == null ? "" : leaveType.trim();
        String selectedStatus = status == null ? "" : status.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<LeaveInfo> leavePage = leaveService.findAdminPage(
                typeKeyword,
                selectedStatus,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (leavePage.getTotalPages() > 0 && pageNumber > leavePage.getTotalPages()) {
            pageNumber = leavePage.getTotalPages();
            leavePage = leaveService.findAdminPage(
                    typeKeyword,
                    selectedStatus,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("leaves", leavePage.getContent());
        model.addAttribute("leavePage", leavePage);
        model.addAttribute("leaveType", typeKeyword);
        model.addAttribute("status", selectedStatus);
        model.addAttribute("statusOptions", Arrays.asList("待审批", "已通过", "已驳回"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", leavePage.getTotalPages());
        model.addAttribute("totalElements", leavePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= leavePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
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


    @PostMapping("/batch-delete")
    public String batchDelete(@RequestParam(required = false) List<Long> ids,
                              @RequestParam(required = false) String leaveType,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) Integer page,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/leave";
        }
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
        if (leaveType != null) redirectAttributes.addAttribute("leaveType", leaveType);
        if (status != null) redirectAttributes.addAttribute("status", status);
        if (page != null) redirectAttributes.addAttribute("page", page);
            return "redirect:/leave";
        }
        try {
            int count = leaveService.batchDeleteByIds(ids);
            if (count == 0) {
                redirectAttributes.addFlashAttribute("error", "\u8bf7\u81f3\u5c11\u9009\u62e9\u4e00\u6761\u8bb0\u5f55");
            } else {
                redirectAttributes.addFlashAttribute("message", "\u6210\u529f\u5220\u9664 " + count + " \u6761\u8bb0\u5f55");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "\u6279\u91cf\u5220\u9664\u5931\u8d25\uff0c\u90e8\u5206\u8bb0\u5f55\u53ef\u80fd\u5b58\u5728\u5173\u8054\u6570\u636e");
        }
        if (leaveType != null) redirectAttributes.addAttribute("leaveType", leaveType);
        if (status != null) redirectAttributes.addAttribute("status", status);
        if (page != null) redirectAttributes.addAttribute("page", page);
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
    public String myLeaves(@RequestParam(required = false) String leaveType,
                           @RequestParam(required = false) String status,
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
        String typeKeyword = leaveType == null ? "" : leaveType.trim();
        String selectedStatus = status == null ? "" : status.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<LeaveInfo> leavePage = leaveService.findStudentPage(
                studentId,
                typeKeyword,
                selectedStatus,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (leavePage.getTotalPages() > 0 && pageNumber > leavePage.getTotalPages()) {
            pageNumber = leavePage.getTotalPages();
            leavePage = leaveService.findStudentPage(
                    studentId,
                    typeKeyword,
                    selectedStatus,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("leaves", leavePage.getContent());
        model.addAttribute("leavePage", leavePage);
        model.addAttribute("leaveType", typeKeyword);
        model.addAttribute("status", selectedStatus);
        model.addAttribute("statusOptions", Arrays.asList("待审批", "已通过", "已驳回"));
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", leavePage.getTotalPages());
        model.addAttribute("totalElements", leavePage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= leavePage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
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