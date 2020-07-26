package com.studithm.modules.study;

import com.studithm.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

    private final StudyService studyService;

    public Study creatStudy(String path, Account manager) {
        Study newStudy = new Study();
        newStudy.setPath(path);
        studyService.createNewStudy(newStudy, manager);
        return newStudy;
    }

}
