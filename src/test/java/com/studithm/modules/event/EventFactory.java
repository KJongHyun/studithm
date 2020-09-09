package com.studithm.modules.event;

import com.studithm.modules.account.Account;
import com.studithm.modules.Gathering.Gathering;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventFactory {

    private final EventService eventService;

    public Event createEvent(Gathering gathering, Account createBy, String title, int limit, EventType eventType) {
        Event event = new Event();
        event.setTitle(title);
        event.setLimitOfEnrollments(limit);
        event.setEventType(eventType);
        event.setCreatDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        eventService.createEvent(event, gathering, createBy);

        return event;
    }

}
