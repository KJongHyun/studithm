package com.studithm.modules.account.form;

import lombok.Data;

@Data
public class Notifications {
    private boolean gatheringCreatedByEmail;

    private boolean gatheringCreatedByWeb;

    private boolean gatheringEnrollmentResultByEmail;

    private boolean gatheringEnrollmentResultByWeb;

    private boolean gatheringUpdatedByEmail;

    private boolean gatheringUpdatedByWeb;
}
