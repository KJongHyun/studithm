package com.lightning.modules.gathering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightning.infra.AbstractContainerBaseTest;
import com.lightning.infra.MockMvcTest;
import com.lightning.modules.account.WithAccount;
import com.lightning.modules.account.Account;
import com.lightning.modules.account.AccountRepository;
import com.lightning.modules.gathering.form.GatheringForm;
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
    @DisplayName("모임 개설 폼 조회")
    @Test
    void createGatheringForm() throws Exception {
        mockMvc.perform(get("/new-gathering"))
                .andExpect(status().isOk())
                .andExpect(view().name("gathering/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("gatheringForm"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("모임 개설 완료")
    @Test
    void createGathering_success() throws Exception {
        GatheringForm gatheringForm = new GatheringForm();
        gatheringForm.setPath("testgathering");
        gatheringForm.setTitle("testTitle");
        gatheringForm.setShortDescription("testShortDescription");
        gatheringForm.setFullDescription("testFullDescription");

        mockMvc.perform(post("/new-gathering")
                .param("path",  gatheringForm.getPath())
                .param("title", gatheringForm.getTitle())
                .param("shortDescription", gatheringForm.getShortDescription())
                .param("fullDescription", gatheringForm.getFullDescription())
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gathering/" + gatheringForm.getPath()));

        Gathering gathering =  gatheringRepository.findByPath(gatheringForm.getPath());

        assertNotNull(gathering);
        Account account = accountRepository.findByNickname("jonghyeon1");
        assertTrue(gathering.getManagers().contains(account));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("모임 개설 실패")
    @Test
    void creatGathering_fail() throws Exception {
        mockMvc.perform(post("/new-gathering")
                .param("path",  "wrong path test")
                .param("title","gathering title")
                .param("shortDescription", "short Description")
                .param("fullDescription", "full Description")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("gatheringForm"))
                .andExpect(view().name("gathering/form"));

        Gathering gathering = gatheringRepository.findByPath("wrong path test");
        assertNull(gathering);
    }

    @WithAccount("jonghyeon1")
    @DisplayName("모임 조회")
    void viewGathering() throws Exception {
        String path = "test-path";

        Account account = accountRepository.findByNickname("jonghyeon1");
        gatheringFactory.creatGathering(path, account);

        mockMvc.perform(get("/gathering/" + path))
                .andExpect(view().name("gathering/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("gathering"));

    }

}