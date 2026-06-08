package com.example.student.service;

import com.example.student.entity.StudentInfo;
import com.example.student.entity.UserAccount;
import com.example.student.repository.StudentInfoRepository;
import com.example.student.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final StudentInfoRepository studentInfoRepository;

    public UserService(UserAccountRepository userAccountRepository,
                       StudentInfoRepository studentInfoRepository) {
        this.userAccountRepository = userAccountRepository;
        this.studentInfoRepository = studentInfoRepository;
    }

    public UserAccount login(String username, String password) {
        UserAccount user = userAccountRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password) && user.getStatus() == 1) {
            return user;
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    public boolean isStudentNoExists(String studentNo) {
        return studentInfoRepository.existsByStudentNo(studentNo);
    }

    @Transactional
    public void register(String username, String password, String studentNo, String name) {
        StudentInfo studentInfo = new StudentInfo();
        studentInfo.setStudentNo(studentNo);
        studentInfo.setName(name);
        studentInfo.setStatus(1);
        studentInfo.setCreateTime(LocalDateTime.now());
        studentInfo = studentInfoRepository.save(studentInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(username);
        userAccount.setPassword(password);
        userAccount.setRole("STUDENT");
        userAccount.setRelatedStudentId(studentInfo.getId());
        userAccount.setStatus(1);
        userAccount.setCreateTime(LocalDateTime.now());
        userAccountRepository.save(userAccount);
    }
}
