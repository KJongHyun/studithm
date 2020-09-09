package com.studithm.modules.event;

import com.studithm.modules.account.Account;
import com.studithm.modules.event.event.EnrollmentAcceptedEvent;
import com.studithm.modules.event.event.EnrollmentRejectEvent;
import com.studithm.modules.event.form.EventForm;
import com.studithm.modules.Gathering.Gathering;
import com.studithm.modules.Gathering.event.GatheringUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Event createEvent(Event event, Gathering gathering, Account account) {
        event.setCreatedBy(account);
        event.setCreatDateTime(LocalDateTime.now());
        event.setGathering(gathering);
        eventPublisher.publishEvent(new GatheringUpdateEvent(event.getGathering(), "'" + event.getTitle() + "' 모임을 만들었습니다." ));
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        eventPublisher.publishEvent(new GatheringUpdateEvent(event.getGathering(), "'" + event.getTitle() + "' 모임 정보를 수정했으니 확인하세요." ));
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new GatheringUpdateEvent(event.getGathering(), "'" + event.getTitle() + "' 모임을 취소했습니다." ));
    }

    public void newEnrollment(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (enrollment == null) {
            throw new IllegalArgumentException("잘못된 참가 정보 입니다.");
        }
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectEvent(enrollment));
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
