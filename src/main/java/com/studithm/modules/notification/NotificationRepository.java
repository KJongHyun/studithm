package com.studithm.modules.notification;

import com.studithm.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByAccountAndChecked(Account account, boolean checked);
}
