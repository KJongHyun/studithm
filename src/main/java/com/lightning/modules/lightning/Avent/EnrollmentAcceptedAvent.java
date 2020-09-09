package com.lightning.modules.lightning.Avent;

import com.lightning.modules.lightning.Enrollment;

public class EnrollmentAcceptedAvent extends EnrollmentAvent {

    public EnrollmentAcceptedAvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 확인했습니다. 모임에 참석하세요.");
    }
}
