package com.studithm.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studithm.WithAccount;
import com.studithm.account.AccountRepository;
import com.studithm.account.AccountService;
import com.studithm.domain.Account;
import com.studithm.domain.Zone;
import com.studithm.settings.form.ZoneForm;
import com.studithm.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studithm.settings.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ZoneRepository zoneRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountService accountService;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    public void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("jonghyeon1")
    @DisplayName("계정의 지역 정보 수정 폼")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("계정의 지역 정보 추가")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account account = accountRepository.findByNickname("jonghyeon1");
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        assertTrue(account.getZones().contains(zone));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("계정의 지역 정보 삭제")
    @Test
    void removeZone() throws Exception {
        Account account = accountRepository.findByNickname("jonghyeon1");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(account, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(account.getZones().contains(zone));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        String shortBio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile_with_correct_input() throws Exception {
        String shortBio = "짧은 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", shortBio)
                 .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account jonghyeon = accountRepository.findByNickname("jonghyeon1");
        assertEquals(shortBio, jonghyeon.getBio());
    }

    @WithAccount("jonghyeon1")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_with_error_input() throws Exception {
        String shortBio = "길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우. 길게 소개를 수정하는 경우.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", shortBio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account jonghyeon = accountRepository.findByNickname("jonghyeon1");
        assertNull(jonghyeon.getBio());
    }

    @WithAccount("jonghyeon1")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertTrue(passwordEncoder.matches("12345678", jonghyeon1.getPassword()));
    }

    @WithAccount("jonghyeon1")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

}