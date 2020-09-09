package com.lightning.modules.lightning;

import com.lightning.modules.account.Account;
import com.lightning.modules.Gathering.Gathering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventFactory {

    private final LightningService lightningService;

    public Lightning createEvent(Gathering gathering, Account createBy, String title, int limit, LightningType lightningType) {
        Lightning lightning = new Lightning();
        lightning.setTitle(title);
        lightning.setLimitOfEnrollments(limit);
        lightning.setLightningType(lightningType);
        lightning.setCreatDateTime(LocalDateTime.now());
        lightning.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        lightning.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        lightning.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        lightningService.createLightning(lightning, gathering, createBy);

        return lightning;
    }

}
