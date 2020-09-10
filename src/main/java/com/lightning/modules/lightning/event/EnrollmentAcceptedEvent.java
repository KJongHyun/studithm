package com.lightning.modules.lightning.event;

import com.lightning.modules.lightning.Enrollment;

public class EnrollmentAcceptedEvent extends EnrollmentEvent {

    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "번개 참가 신청을 확인했습니다. 번개에 참석하세요.");
    }
}
