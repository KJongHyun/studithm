package com.studithm.modules.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studithm.modules.account.WithAccount;
import com.studithm.modules.account.Account;
import com.studithm.modules.account.AccountRepository;
import com.studithm.modules.study.form.StudyForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyService studyService;

    @AfterEach
    void afterEach() {
        studyRepository.deleteAll();
    }

    @WithAccount("jonghyeon1")
    @DisplayName("스터디 개설 폼 조회")
    @Test
    void createStudyForm() throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("스터디 개설 완료")
    @Test
    void createStudy_success() throws Exception {
        StudyForm studyForm = new StudyForm();
        studyForm.setPath("teststudy");
        studyForm.setTitle("testTitle");
        studyForm.setShortDescription("testShortDescription");
        studyForm.setFullDescription("testFullDescription");

        mockMvc.perform(post("/new-study")
                .param("path",  studyForm.getPath())
                .param("title", studyForm.getTitle())
                .param("shortDescription", studyForm.getShortDescription())
                .param("fullDescription", studyForm.getFullDescription())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyForm.getPath()));

        Study study =  studyRepository.findByPath(studyForm.getPath());

        assertNotNull(study);
        Account account = accountRepository.findByNickname("jonghyeon1");
        assertTrue(study.getManagers().contains(account));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("스터디 개설 실패")
    @Test
    void creatStudy_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path",  "wrong path test")
                .param("title","study title")
                .param("shortDescription", "short Description")
                .param("fullDescription", "full Description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("studyForm"))
                .andExpect(view().name("study/form"));

        Study study = studyRepository.findByPath("wrong path test");
        assertNull(study);
    }

    @WithAccount("jonghyeon1")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        Study study = new Study();
        study.setPath("test-path");
        study.setTitle("test study");
        study.setShortDescription("short description");
        study.setFullDescription("<p>full description</p>");

        Account account = accountRepository.findByNickname("jonghyeon1");
        studyService.createNewStudy(study, account);

        mockMvc.perform(get("/study/" + study.getPath()))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

    }

}