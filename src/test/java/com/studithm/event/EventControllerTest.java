package com.studithm.event;

import com.studithm.WithAccount;
import com.studithm.account.AccountRepository;
import com.studithm.domain.Account;
import com.studithm.domain.Event;
import com.studithm.domain.EventType;
import com.studithm.domain.Study;
import com.studithm.study.StudyRepository;
import com.studithm.study.StudyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired StudyRepository studyRepository;
    @Autowired EventRepository eventRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired StudyService studyService;
    @Autowired EventService eventService;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("jonghyeon1")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account jonghyeon = createAccount("jonghyeon3");
        Study study = creatStudy("test-study", jonghyeon);
        Event event = createEvent(study, jonghyeon, "test-event", 2, EventType.FCFS);

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
        Account jonghyeon2 = createAccount("jonghyeon2");
        Account jonghyeon3 = createAccount("jonghyeon3");
        Account jonghyeon4 = createAccount("jonghyeon4");
        Study study = creatStudy("test-study", jonghyeon2);
        Event event = createEvent(study, jonghyeon2, "test-event", 2, EventType.FCFS);
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

        Account jonghyeon2 = createAccount("jonghyeon2");
        Study study = creatStudy("test-study", jonghyeon2);
        Event event = createEvent(study, jonghyeon2, "test-event", 2, EventType.CONFIRMATIVE);
        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account jonghyeon1 = accountRepository.findByNickname("jonghyeon1");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, jonghyeon1).isAccepted());
    }

    protected Account createAccount(String nickname) {
        Account newAccount = new Account();
        newAccount.setNickname(nickname);
        newAccount.setEmail(nickname + "@naver.com");
        accountRepository.save(newAccount);
        return newAccount;
    }

    protected Study creatStudy(String path, Account manager) {
        Study newStudy = new Study();
        newStudy.setPath(path);
        studyService.createNewStudy(newStudy, manager);
        return newStudy;
    }

    protected Event createEvent(Study study, Account createBy, String title, int limit, EventType eventType) {
        Event event = new Event();
        event.setTitle(title);
        event.setLimitOfEnrollments(limit);
        event.setEventType(eventType);
        event.setCreatDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        eventService.createEvent(event, study, createBy);

        return event;
    }
}