package com.studithm.modules.study.event;

import com.studithm.modules.study.Study;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StudyCreatedEvent {

    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
