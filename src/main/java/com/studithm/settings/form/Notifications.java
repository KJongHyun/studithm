package com.studithm.settings.form;

import com.studithm.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Notifications {
    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
