package com.studithm.modules.main;

import com.studithm.modules.account.Account;
import com.studithm.modules.account.AccountRepository;
import com.studithm.modules.account.CurrentAccount;
import com.studithm.modules.event.Enrollment;
import com.studithm.modules.event.EnrollmentRepository;
import com.studithm.modules.study.Study;
import com.studithm.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final EnrollmentRepository enrollmentRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            Account loginAccount = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(loginAccount);

            List<Enrollment> enrollmentList = enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAt(loginAccount, true);
            model.addAttribute("enrollmentList", enrollmentList);

            List<Study> studyList = studyRepository.findByTagsAndZones(loginAccount.getTags(), loginAccount.getZones());
            model.addAttribute("studyList", studyList);


            model.addAttribute("manageStudyList", studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("memberStudyList", studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }

        List<Study> studyList = studyRepository.findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);
        model.addAttribute("studyList", studyList);

        return "index";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/search/study") //TODO size, page, sort
    public String searchStudy(String keyword, Model model,
            @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}
