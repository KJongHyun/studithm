package com.studithm.modules.event.event;

import com.studithm.infra.config.AppProperties;
import com.studithm.infra.mail.EmailMessage;
import com.studithm.infra.mail.EmailService;
import com.studithm.modules.account.Account;
import com.studithm.modules.event.Enrollment;
import com.studithm.modules.event.Event;
import com.studithm.modules.notification.Notification;
import com.studithm.modules.notification.NotificationRepository;
import com.studithm.modules.notification.NotificationType;
import com.studithm.modules.Gathering.Gathering;
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
        Event event = enrollment.getEvent();
        Gathering gathering = event.getGathering();

        if (account.isGatheringEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, gathering);
        }

        if (account.isGatheringEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, event, gathering);
        }

    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Gathering gathering) {
        Notification notification = new Notification();
        notification.setTitle(gathering.getTitle());
        notification.setLink("/gathering/" + gathering.getEncodedPath() + "/events" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Gathering gathering) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/gathering/" + gathering.getEncodedPath() + "/events" + event.getId());
        context.setVariable("linkName", gathering.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("라이트닝, " + event.getTitle() + " 모임 참가 신청 결과 입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
