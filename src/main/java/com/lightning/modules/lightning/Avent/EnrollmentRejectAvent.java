package com.lightning.modules.lightning.Avent;

import com.lightning.modules.lightning.Enrollment;

public class EnrollmentRejectAvent extends EnrollmentAvent {

    public EnrollmentRejectAvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 거절했습니다.");
    }
}
