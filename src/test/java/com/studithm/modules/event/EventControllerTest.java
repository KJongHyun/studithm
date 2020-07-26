package com.studithm.modules.event;

import com.studithm.infra.AbstractContainerBaseTest;
import com.studithm.infra.MockMvcTest;
import com.studithm.modules.account.Account;
import com.studithm.modules.account.AccountFactory;
import com.studithm.modules.account.AccountRepository;
import com.studithm.modules.account.WithAccount;
import com.studithm.modules.study.Study;
import com.studithm.modules.study.StudyFactory;
import com.studithm.modules.study.StudyRepository;
import com.studithm.modules.study.StudyService;
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
class EventControllerTest extends AbstractContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired EventRepository eventRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired StudyService studyService;
    @Autowired EventService eventService;
    @Autowired EventFactory eventFactory;
    @Autowired StudyFactory studyFactory;
    @Autowired AccountFactory accountFactory;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("jonghyeon1")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account jonghyeon = accountFactory.createAccount("jonghyeon3");
        Study study = studyFactory.creatStudy("test-study", jonghyeon);
        Event event = eventFactory.createEvent(study, jonghyeon, "test-event", 2, EventType.FCFS);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertTrue(enrollmentRepository.existsByEventAndAccount(event, jonghyeon1));
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 취소")
    @WithAccount("jonghyeon1")
    void cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        Account jonghyeon2 = accountFactory.createAccount("jonghyeon2");
        Account jonghyeon3 = accountFactory.createAccount("jonghyeon3");
        Account jonghyeon4 = accountFactory.createAccount("jonghyeon4");
        Study study = studyFactory.creatStudy("test-study", jonghyeon2);
        Event event = eventFactory.createEvent(study, jonghyeon2, "test-event", 2, EventType.FCFS);
        eventService.newEnrollment(event, jonghyeon1);
        eventService.newEnrollment(event, jonghyeon3);
        eventService.newEnrollment(event, jonghyeon4);

        assertTrue(enrollmentRepository.findByEventAndAccount(event, jonghyeon1).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, jonghyeon3).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event, jonghyeon4).isAccepted());

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event, jonghyeon3).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, jonghyeon4).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, jonghyeon1));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("jonghyeon1")
    void newEnrollment_to_CONFIRMATIVE_event_not_accepted() throws Exception {

        Account jonghyeon2 = accountFactory.createAccount("jonghyeon2");
        Study study = studyFactory.creatStudy("test-study", jonghyeon2);
        Event event = eventFactory.createEvent(study, jonghyeon2, "test-event", 2, EventType.CONFIRMATIVE);
        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, jonghyeon1).isAccepted());
    }

}