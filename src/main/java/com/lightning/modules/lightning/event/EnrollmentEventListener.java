package com.lightning.modules.lightning.event;

import com.lightning.infra.config.AppProperties;
import com.lightning.infra.mail.EmailMessage;
import com.lightning.infra.mail.EmailService;
import com.lightning.modules.account.Account;
import com.lightning.modules.lightning.Enrollment;
import com.lightning.modules.lightning.Lightning;
import com.lightning.modules.gathering.Gathering;
import com.lightning.modules.notification.Notification;
import com.lightning.modules.notification.NotificationRepository;
import com.lightning.modules.notification.NotificationType;
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
public class EnrollmentEventListener {

    private final EmailService emailService;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;
    private final TemplateEngine templateEngine;

    @EventListener
    void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Lightning lightning = enrollment.getLightning();
        Gathering gathering = lightning.getGathering();

        if (account.isGatheringEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, lightning, gathering);
        }

        if (account.isGatheringEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, lightning, gathering);
        }

    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Lightning lightning, Gathering gathering) {
        Notification notification = new Notification();
        notification.setTitle(gathering.getTitle());
        notification.setLink("/gathering/" + gathering.getEncodedPath() + "/events" + lightning.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Lightning lightning, Gathering gathering) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/gathering/" + gathering.getEncodedPath() + "/events" + lightning.getId());
        context.setVariable("linkName", gathering.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("롸이트닝, " + lightning.getTitle() + " 번개 참가 신청 결과 입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
