package com.studithm.modules.Gathering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studithm.infra.AbstractContainerBaseTest;
import com.studithm.infra.MockMvcTest;
import com.studithm.modules.account.WithAccount;
import com.studithm.modules.account.Account;
import com.studithm.modules.account.AccountRepository;
import com.studithm.modules.Gathering.form.GatheringForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class GatheringControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    GatheringRepository gatheringRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired
    GatheringService gatheringService;
    @Autowired
    GatheringFactory gatheringFactory;

    @AfterEach
    void afterEach() {
        gatheringRepository.deleteAll();
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
        GatheringForm gatheringForm = new GatheringForm();
        gatheringForm.setPath("teststudy");
        gatheringForm.setTitle("testTitle");
        gatheringForm.setShortDescription("testShortDescription");
        gatheringForm.setFullDescription("testFullDescription");

        mockMvc.perform(post("/new-study")
                .param("path",  gatheringForm.getPath())
                .param("title", gatheringForm.getTitle())
                .param("shortDescription", gatheringForm.getShortDescription())
                .param("fullDescription", gatheringForm.getFullDescription())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + gatheringForm.getPath()));

        Gathering gathering =  gatheringRepository.findByPath(gatheringForm.getPath());

        assertNotNull(gathering);
        Account account = accountRepository.findByNickname("jonghyeon1");
        assertTrue(gathering.getManagers().contains(account));
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

        Gathering gathering = gatheringRepository.findByPath("wrong path test");
        assertNull(gathering);
    }

    @WithAccount("jonghyeon1")
    @DisplayName("스터디 조회")
    void viewStudy() throws Exception {
        String path = "test-path";

        Account account = accountRepository.findByNickname("jonghyeon1");
        gatheringFactory.creatGathering(path, account);

        mockMvc.perform(get("/study/" + path))
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));

    }

}