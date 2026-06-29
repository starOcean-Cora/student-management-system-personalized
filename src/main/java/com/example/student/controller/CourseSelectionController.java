package com.example.student.controller;

import com.example.student.entity.CourseInfo;
import com.example.student.entity.CourseSelection;
import com.example.student.repository.CourseInfoRepository;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.service.CourseSelectionService;
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
    public String list(@RequestParam(required = false) String studentName,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(defaultValue = "1") Integer page,
                       HttpSession session,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        String keyword = studentName == null ? "" : studentName.trim();
        int pageNumber = page == null || page < 1 ? 1 : page;
        Page<CourseSelection> selectionPage = selectionService.findAdminPage(
                keyword,
                courseId,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (selectionPage.getTotalPages() > 0 && pageNumber > selectionPage.getTotalPages()) {
            pageNumber = selectionPage.getTotalPages();
            selectionPage = selectionService.findAdminPage(
                    keyword,
                    courseId,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }
        model.addAttribute("selections", selectionPage.getContent());
        model.addAttribute("selectionPage", selectionPage);
        model.addAttribute("studentName", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courses", courseInfoRepository.findAll());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", selectionPage.getTotalPages());
        model.addAttribute("totalElements", selectionPage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= selectionPage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "selection/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
                redirectAttributes.addFlashAttribute("error", "\u8be5\u5b66\u751f\u5df2\u9009\u62e9\u6b64\u8bfe\u7a0b");
                return "redirect:/selection/add";
            }
            selectionService.selectCourse(selection.getStudentId(), selection.getCourseId());
        }
        redirectAttributes.addFlashAttribute("message", "\u4fdd\u5b58\u6210\u529f");
        return "redirect:/selection";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
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
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/selection";
        }
        selectionService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "\u5220\u9664\u6210\u529f");
        return "redirect:/selection";
    }

    @GetMapping("/my")
    public String mySelections(@RequestParam(required = false) String courseName,
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
        Page<CourseSelection> mySelectionPage = selectionService.findStudentPage(
                studentId,
                keyword,
                courseId,
                PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
        );
        if (mySelectionPage.getTotalPages() > 0 && pageNumber > mySelectionPage.getTotalPages()) {
            pageNumber = mySelectionPage.getTotalPages();
            mySelectionPage = selectionService.findStudentPage(
                    studentId,
                    keyword,
                    courseId,
                    PageRequest.of(pageNumber - 1, 10, Sort.by(Sort.Direction.ASC, "id"))
            );
        }

        List<CourseSelection> allMySelections = selectionService.findByStudentId(studentId);
        List<CourseInfo> allCourses = courseInfoRepository.findAll();
        Set<Long> selectedCourseIds = allMySelections.stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toSet());

        List<CourseInfo> availableCourses = new ArrayList<>();
        for (CourseInfo c : allCourses) {
            if (!selectedCourseIds.contains(c.getId())) {
                availableCourses.add(c);
            }
        }

        model.addAttribute("mySelections", mySelectionPage.getContent());
        model.addAttribute("mySelectionPage", mySelectionPage);
        model.addAttribute("availableCourses", availableCourses);
        model.addAttribute("courses", allCourses);
        model.addAttribute("courseName", keyword);
        model.addAttribute("courseId", courseId);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", mySelectionPage.getTotalPages());
        model.addAttribute("totalElements", mySelectionPage.getTotalElements());
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 1; i <= mySelectionPage.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        model.addAttribute("pageNumbers", pageNumbers);
        return "selection/my";
    }

    @GetMapping("/select/{courseId}")
    public String selectCourse(@PathVariable Long courseId,
                               HttpSession session,
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
        if (selectionService.isAlreadySelected(studentId, courseId)) {
            redirectAttributes.addFlashAttribute("error", "\u4e0d\u80fd\u91cd\u590d\u9009\u8bfe");
            return "redirect:/selection/my";
        }
        selectionService.selectCourse(studentId, courseId);
        redirectAttributes.addFlashAttribute("message", "\u9009\u8bfe\u6210\u529f");
        return "redirect:/selection/my";
    }

    @GetMapping("/cancel/{id}")
    public String cancelSelection(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!"STUDENT".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/";
        }
        Long studentId = (Long) session.getAttribute("relatedStudentId");
        CourseSelection selection = selectionService.findById(id).orElse(null);
        if (selection == null || !selection.getStudentId().equals(studentId)) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u53d6\u6d88\u6b64\u9009\u8bfe");
            return "redirect:/selection/my";
        }
        selectionService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "\u5df2\u53d6\u6d88\u9009\u8bfe");
        return "redirect:/selection/my";
    }

    @GetMapping("/repair")
    public String repair(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!"ADMIN".equals(session.getAttribute("role"))) {
            redirectAttributes.addFlashAttribute("error", "\u6ca1\u6709\u6743\u9650\u6267\u884c\u6b64\u64cd\u4f5c");
            return "redirect:/selection";
        }
        int count = selectionService.repairAllNullClasses();
        redirectAttributes.addFlashAttribute("message", "\u5df2\u4fee\u590d " + count + " \u6761\u9009\u8bfe\u8bb0\u5f55\u7684\u73ed\u7ea7\u4fe1\u606f");
        return "redirect:/selection";
    }
}
