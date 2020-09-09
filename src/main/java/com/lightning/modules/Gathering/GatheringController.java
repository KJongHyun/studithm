package com.lightning.modules.Gathering;

import com.lightning.modules.Gathering.form.GatheringForm;
import com.lightning.modules.account.Account;
import com.lightning.modules.account.CurrentAccount;
import com.lightning.modules.Gathering.validator.GatheringFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;
    private final ModelMapper modelMapper;
    private final GatheringFormValidator gatheringFormValidator;
    private final GatheringRepository gatheringRepository;


    @InitBinder("gatheringForm")
    public void gatheringFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(gatheringFormValidator);
    }

    @GetMapping("/gathering/{path}")
    public String viewGathering(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGathering(path);
        model.addAttribute(account);
        model.addAttribute(gathering);

        return "gathering/view";
    }


    @GetMapping("/new-gathering")
    public String newGatheringForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new GatheringForm());
        return "gathering/form";
    }

    @PostMapping("/new-gathering")
    public String newGatheringSubmit(@CurrentAccount Account account, @Valid GatheringForm gatheringForm, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "gathering/form";
        }

        Gathering newGathering = gatheringService.createNewGathering(modelMapper.map(gatheringForm, Gathering.class), account);

        return "redirect:/gathering/" + URLEncoder.encode(newGathering.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/gathering/{path}/members")
    public String viewGatheringMembers(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGathering(path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        return "gathering/members";
    }

    @PostMapping("/gathering/{path}/join")
    public String joinGathering(@CurrentAccount Account account, @PathVariable String path) {
        Gathering gathering = gatheringRepository.findGatheringWithMembersByPath(path);
        gatheringService.addMember(gathering, account);

        return "redirect:/gathering/" + gathering.getEncodedPath() + "/members";
    }

    @PostMapping("/gathering/{path}/leave")
    public String leaveGathering(@CurrentAccount Account account, @PathVariable String path) {
        Gathering gathering = gatheringRepository.findGatheringWithMembersByPath(path);
        gatheringService.removeMember(gathering, account);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/members";
    }

    @GetMapping("/gathering/data")
    public String generateTestData(@CurrentAccount Account account) {
        gatheringService.generateTestGatherings(account);
        return "redirect:/";
    }
}
