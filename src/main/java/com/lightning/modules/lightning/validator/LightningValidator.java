package com.lightning.modules.lightning.validator;

import com.lightning.modules.lightning.Lightning;
import com.lightning.modules.lightning.form.LightningForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class LightningValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return LightningForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        LightningForm lightningForm = (LightningForm) target;

        if (isNotValidEndEnrollmentDateTime(lightningForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.datetime", "번개 접수 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidEndDateTime(lightningForm)) {
            errors.rejectValue("endDateTime", "wrong.datetime", "번개 접수 종료 일시를 정확히 입력하세요.");
        }

        if (isNotValidStartDateTime(lightningForm)) {
            errors.rejectValue("startDateTime", "wrong.datetime", "번개 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidStartDateTime(LightningForm lightningForm) {
        return lightningForm.getStartDateTime().isBefore(lightningForm.getEndEnrollmentDateTime());
    }

    private boolean isNotValidEndEnrollmentDateTime(LightningForm lightningForm) {
        return lightningForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isNotValidEndDateTime(LightningForm lightningForm) {
        return lightningForm.getEndDateTime().isBefore(lightningForm.getStartDateTime()) || lightningForm.getEndDateTime().isBefore(lightningForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateForm(LightningForm lightningForm, Lightning lightning, Errors errors) {
        if (lightningForm.getLimitOfEnrollments() < lightning.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
