package com.lightning.modules.lightning.Avent;

import com.lightning.infra.config.AppProperties;
import com.lightning.infra.mail.EmailMessage;
import com.lightning.infra.mail.EmailService;
import com.lightning.modules.account.Account;
import com.lightning.modules.lightning.Enrollment;
import com.lightning.modules.lightning.Lightning;
import com.lightning.modules.notification.Notification;
import com.lightning.modules.notification.NotificationRepository;
import com.lightning.modules.notification.NotificationType;
import com.lightning.modules.Gathering.Gathering;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class EnrollmentAventListener {

    private final EmailService emailService;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;
    private final TemplateEngine templateEngine;

    @EventListener
    void handleEnrollmentEvent(EnrollmentAvent enrollmentAvent) {
        Enrollment enrollment = enrollmentAvent.getEnrollment();
        Account account = enrollment.getAccount();
        Lightning lightning = enrollment.getLightning();
        Gathering gathering = lightning.getGathering();

        if (account.isGatheringEnrollmentResultByEmail()) {
            sendEmail(enrollmentAvent, account, lightning, gathering);
        }

        if (account.isGatheringEnrollmentResultByWeb()) {
            createNotification(enrollmentAvent, account, lightning, gathering);
        }

    }

    private void createNotification(EnrollmentAvent enrollmentAvent, Account account, Lightning lightning, Gathering gathering) {
        Notification notification = new Notification();
        notification.setTitle(gathering.getTitle());
        notification.setLink("/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightning.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentAvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

    private void sendEmail(EnrollmentAvent enrollmentAvent, Account account, Lightning lightning, Gathering gathering) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightning.getId());
        context.setVariable("linkName", gathering.getTitle());
        context.setVariable("message", enrollmentAvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("라이트닝, " + lightning.getTitle() + " 모임 참가 신청 결과 입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
