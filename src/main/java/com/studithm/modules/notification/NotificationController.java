package com.studithm.modules.notification;

import com.studithm.modules.account.Account;
import com.studithm.modules.account.CurrentAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute(account);
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentAccount Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentAccount Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifiactions";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications, long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newGatheringNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingGatheringNotifications = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case GATHERING_CREATED: newGatheringNotifications.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotifications.add(notification); break;
                case GATHERING_UPDATED: watchingGatheringNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newGatheringNotifications", newGatheringNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingGatheringNotifications", watchingGatheringNotifications);
    }

}
