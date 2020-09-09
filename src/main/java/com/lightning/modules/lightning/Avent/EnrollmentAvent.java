package com.lightning.modules.lightning.Avent;

import com.lightning.modules.lightning.Enrollment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class EnrollmentAvent {

    protected final Enrollment enrollment;

    protected final String message;

}
