package com.lightning.modules.lightning;

import com.lightning.modules.account.Account;
import com.lightning.modules.lightning.Avent.EnrollmentAcceptedAvent;
import com.lightning.modules.lightning.Avent.EnrollmentRejectAvent;
import com.lightning.modules.lightning.form.LightningForm;
import com.lightning.modules.Gathering.Gathering;
import com.lightning.modules.Gathering.Avent.GatheringUpdateAvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class LightningService {

    private final LightningRepository lightningRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public Lightning createLightning(Lightning lightning, Gathering gathering, Account account) {
        lightning.setCreatedBy(account);
        lightning.setCreatDateTime(LocalDateTime.now());
        lightning.setGathering(gathering);
        eventPublisher.publishEvent(new GatheringUpdateAvent(lightning.getGathering(), "'" + lightning.getTitle() + "' 모임을 만들었습니다." ));
        return lightningRepository.save(lightning);
    }

    public void updateLightning(Lightning lightning, LightningForm lightningForm) {
        modelMapper.map(lightningForm, lightning);
        eventPublisher.publishEvent(new GatheringUpdateAvent(lightning.getGathering(), "'" + lightning.getTitle() + "' 모임 정보를 수정했으니 확인하세요." ));
        lightning.acceptWaitingList();
    }

    public void deleteLightning(Lightning lightning) {
        lightningRepository.delete(lightning);
        eventPublisher.publishEvent(new GatheringUpdateAvent(lightning.getGathering(), "'" + lightning.getTitle() + "' 모임을 취소했습니다." ));
    }

    public void newEnrollment(Lightning lightning, Account account) {
        if (!enrollmentRepository.existsByLightningAndAccount(lightning, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(lightning.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            lightning.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Lightning lightning, Account account) {
        Enrollment enrollment = enrollmentRepository.findByLightningAndAccount(lightning, account);
        if (enrollment == null) {
            throw new IllegalArgumentException("잘못된 참가 정보 입니다.");
        }
        if (!enrollment.isAttended()) {
            lightning.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            lightning.acceptNextWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Lightning lightning, Enrollment enrollment) {
        lightning.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedAvent(enrollment));
    }

    public void rejectEnrollment(Lightning lightning, Enrollment enrollment) {
        lightning.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectAvent(enrollment));
    }

    public void checkInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
