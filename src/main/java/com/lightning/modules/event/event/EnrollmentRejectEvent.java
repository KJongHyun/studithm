package com.lightning.modules.event.event;

import com.lightning.modules.event.Enrollment;

public class EnrollmentRejectEvent extends EnrollmentEvent {

    public EnrollmentRejectEvent(Enrollment enrollment) {
        super(enrollment, "번개 참가 신청을 거절했습니다.");
    }
}
