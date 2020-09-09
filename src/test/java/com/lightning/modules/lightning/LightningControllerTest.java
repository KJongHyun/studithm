package com.lightning.modules.lightning;

import com.lightning.infra.AbstractContainerBaseTest;
import com.lightning.infra.MockMvcTest;
import com.lightning.modules.account.Account;
import com.lightning.modules.account.AccountFactory;
import com.lightning.modules.account.AccountRepository;
import com.lightning.modules.account.WithAccount;
import com.lightning.modules.Gathering.Gathering;
import com.lightning.modules.Gathering.GatheringFactory;
import com.lightning.modules.Gathering.GatheringRepository;
import com.lightning.modules.Gathering.GatheringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockMvcTest
class LightningControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired
    GatheringRepository gatheringRepository;
    @Autowired
    LightningRepository lightningRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired
    GatheringService gatheringService;
    @Autowired
    LightningService lightningService;
    @Autowired EventFactory eventFactory;
    @Autowired
    GatheringFactory gatheringFactory;
    @Autowired AccountFactory accountFactory;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("jonghyeon1")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account jonghyeon = accountFactory.createAccount("jonghyeon3");
        Gathering gathering = gatheringFactory.creatGathering("test-gathering", jonghyeon);
        Lightning lightning = eventFactory.createEvent(gathering, jonghyeon, "test-event", 2, LightningType.FCFS);

        mockMvc.perform(post("/gathering/" + gathering.getPath() + "/events/" + lightning.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gathering/" + gathering.getPath() + "/events/" + lightning.getId()));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertTrue(enrollmentRepository.existsByLightningAndAccount(lightning, jonghyeon1));
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 취소")
    @WithAccount("jonghyeon1")
    void cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        Account jonghyeon2 = accountFactory.createAccount("jonghyeon2");
        Account jonghyeon3 = accountFactory.createAccount("jonghyeon3");
        Account jonghyeon4 = accountFactory.createAccount("jonghyeon4");
        Gathering gathering = gatheringFactory.creatGathering("test-gathering", jonghyeon2);
        Lightning lightning = eventFactory.createEvent(gathering, jonghyeon2, "test-event", 2, LightningType.FCFS);
        lightningService.newEnrollment(lightning, jonghyeon1);
        lightningService.newEnrollment(lightning, jonghyeon3);
        lightningService.newEnrollment(lightning, jonghyeon4);

        assertTrue(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon1).isAccepted());
        assertTrue(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon3).isAccepted());
        assertFalse(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon4).isAccepted());

        mockMvc.perform(post("/gathering/" + gathering.getPath() + "/events/" + lightning.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gathering/" + gathering.getPath() + "/events/" + lightning.getId()));

        assertTrue(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon3).isAccepted());
        assertTrue(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon4).isAccepted());
        assertNull(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon1));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("jonghyeon1")
    void newEnrollment_to_CONFIRMATIVE_event_not_accepted() throws Exception {

        Account jonghyeon2 = accountFactory.createAccount("jonghyeon2");
        Gathering gathering = gatheringFactory.creatGathering("test-gathering", jonghyeon2);
        Lightning lightning = eventFactory.createEvent(gathering, jonghyeon2, "test-event", 2, LightningType.CONFIRMATIVE);
        mockMvc.perform(post("/gathering/" + gathering.getPath() + "/events/" + lightning.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gathering/" + gathering.getPath() + "/events/" + lightning.getId()));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertFalse(enrollmentRepository.findByLightningAndAccount(lightning, jonghyeon1).isAccepted());
    }

}