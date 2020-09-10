package com.lightning.modules.gathering.event;

import com.lightning.infra.config.AppProperties;
import com.lightning.infra.mail.EmailMessage;
import com.lightning.infra.mail.EmailService;
import com.lightning.modules.account.Account;
import com.lightning.modules.account.AccountPredicates;
import com.lightning.modules.account.AccountRepository;
import com.lightning.modules.gathering.Gathering;
import com.lightning.modules.notification.Notification;
import com.lightning.modules.notification.NotificationRepository;
import com.lightning.modules.notification.NotificationType;
import com.lightning.modules.gathering.GatheringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class GatheringEventListener {

    private final GatheringRepository gatheringRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleGatheringCreatedEvent(GatheringCreatedEvent gatheringCreatedEvent) {
        Gathering gathering = gatheringRepository.findGatheringWithTagsAndZonesById(gatheringCreatedEvent.getGathering().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(gathering.getTags(), gathering.getZones()));
        accounts.forEach(account -> {
            if (account.isGatheringCreatedByEmail()) {
                sendGatheringCreatedEmail(gathering, account, "새로운 모임이 생겼습니다.",
                        "롸이트닝, '" + gathering.getTitle() + "' 모임이 생겼습니다.");
            }

            if (account.isGatheringCreatedByWeb()) {
                createNotification(gathering, account, gathering.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleGatheringUpdateEvent(GatheringUpdateEvent gatheringUpdateEvent) {
        Gathering gathering = gatheringRepository.findGatheringWithManagersAndMembersById(gatheringUpdateEvent.getGathering().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(gathering.getManagers());
        accounts.addAll(gathering.getMembers());

        accounts.forEach(account -> {
            if (account.isGatheringUpdatedByEmail()) {
                sendGatheringCreatedEmail(gathering, account, gatheringUpdateEvent.getMessage(),
                        "롸이트닝, '" + gathering.getTitle() + "' 모임에 새소식이 있습니다.");
            }

            if (account.isGatheringUpdatedByWeb()) {
                createNotification(gathering, account, gatheringUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }

        });
    }

    private void createNotification(Gathering gathering, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(gathering.getTitle());
        notification.setLink("/gathering/" + gathering.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void sendGatheringCreatedEmail(Gathering gathering, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/gathering/" + gathering.getEncodedPath());
        context.setVariable("linkName", gathering.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject(emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}
