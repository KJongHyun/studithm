package com.studithm.event;

import com.studithm.domain.Account;
import com.studithm.domain.Event;
import com.studithm.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }
}
