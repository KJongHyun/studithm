package com.studithm.modules.Gathering.validator;

import com.studithm.modules.Gathering.GatheringRepository;
import com.studithm.modules.Gathering.form.GatheringForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class GatheringFormValidator implements Validator {

    private final GatheringRepository gatheringRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return GatheringForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        GatheringForm gatheringForm = (GatheringForm) target;
        if (gatheringRepository.existsByPath(gatheringForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "해당 스터디 경로값을 사용할 수 없습니다.");
        }
    }
}
