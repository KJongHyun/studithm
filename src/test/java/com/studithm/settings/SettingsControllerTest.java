package com.studithm.settings;

import com.studithm.WithAccount;
import com.studithm.account.AccountRepository;
import com.studithm.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    public void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        String shortBio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile_with_correct_input() throws Exception {
        String shortBio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", shortBio)
                 .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account jonghyeon = accountRepository.findByNickname("jonghyeon1");
        assertEquals(shortBio, jonghyeon.getBio());
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_with_error_input() throws Exception {
        String shortBio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우.";
        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", shortBio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account jonghyeon = accountRepository.findByNickname("jonghyeon1");
        assertNull(jonghyeon.getBio());
    }

}