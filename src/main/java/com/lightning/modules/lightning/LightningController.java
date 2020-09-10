package com.lightning.modules.lightning;

import com.lightning.modules.account.Account;
import com.lightning.modules.account.CurrentAccount;
import com.lightning.modules.lightning.form.LightningForm;
import com.lightning.modules.lightning.validator.LightningValidator;
import com.lightning.modules.gathering.Gathering;
import com.lightning.modules.gathering.GatheringService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/gathering/{path}")
@RequiredArgsConstructor
public class LightningController {

    private final GatheringService gatheringService;
    private final LightningService lightningService;
    private final ModelMapper modelMapper;
    private final LightningValidator lightningValidator;
    private final LightningRepository lightningRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("lightningForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(lightningValidator);
    }

    @GetMapping("/new-lightning")
    public String newLightningForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        model.addAttribute(gathering);
        model.addAttribute(account);
        model.addAttribute(new LightningForm());

        return "lightning/form";
    }

    @PostMapping("/new-lightning")
    public String newLightningSubmit(@CurrentAccount Account account, @PathVariable String path,
                                     @Valid LightningForm lightningForm, Errors errors, Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(gathering);
            return "lightning/form";
        }

        Lightning lightning = lightningService.createLightning(modelMapper.map(lightningForm, Lightning.class), gathering, account);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightning.getId();
    }

    @GetMapping("/lightnings/{lightningId}")
    public String getLightning(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long lightningId,
                               Model model) {
        model.addAttribute(account);
        model.addAttribute(lightningRepository.findById(lightningId).orElseThrow());
        model.addAttribute(gatheringService.getGathering(path));

        return "lightning/view";
    }

    @GetMapping("/lightnings")
    public String viewGatheringLightnings(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGathering(path);
        model.addAttribute(account);
        model.addAttribute(gathering);

        List<Lightning> lightnings = lightningRepository.findByGatheringOrderByStartDateTime(gathering);
        List<Lightning> newLightnings = new ArrayList<>();
        List<Lightning> oldLightnings = new ArrayList<>();
        lightnings.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now()))
                oldLightnings.add(e);
            else
                newLightnings.add(e);
        });

        model.addAttribute("newLightnings", newLightnings);
        model.addAttribute("oldLightnings", oldLightnings);

        return "gathering/lightnings";
    }

    @GetMapping("/lightnings/{lightningId}/edit")
    public String updateLightningForm(@CurrentAccount Account account,
                                      @PathVariable String path, @PathVariable Long lightningId, Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        model.addAttribute(gathering);
        model.addAttribute(account);
        model.addAttribute(lightning);
        model.addAttribute(modelMapper.map(lightning, LightningForm.class));
        return "lightning/update-form";
    }

    @PostMapping("/lightnings/{lightningId}/edit")
    public String updateLightningSubmit(@CurrentAccount Account account, @PathVariable String path,
                                        @PathVariable Long lightningId, @Valid LightningForm lightningForm, Errors errors,
                                        Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        lightningForm.setLightningType(lightning.getLightningType());
        lightningValidator.validateUpdateForm(lightningForm, lightning, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(gathering);
            model.addAttribute(lightning);
            return "lightning/update-form";
        }

        lightningService.updateLightning(lightning, lightningForm);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightning.getId();
    }

    @DeleteMapping("/lightnings/{lightningId}")
    public String cancelLightning(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long lightningId) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        lightningService.deleteLightning(lightningRepository.findById(lightningId).orElseThrow());
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings";
    }


    @PostMapping("/lightnings/{lightningId}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long lightningId,
                            Model model) {
        Gathering gathering = gatheringService.getGatheringToEnroll(path);
        lightningService.newEnrollment(lightningRepository.findById(lightningId).orElseThrow(), account);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightningId;
    }

    @PostMapping("/lightnings/{lightningId}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account,
                                   @PathVariable String path, @PathVariable Long lightningId) {
        Gathering gathering = gatheringService.getGatheringToEnroll(path);
        lightningService.cancelEnrollment(lightningRepository.findById(lightningId).orElseThrow(), account);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightningId;
    }

    @PostMapping("/lightnings/{lightningId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable Long lightningId, @PathVariable Long enrollmentId) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        lightningService.acceptEnrollment(lightning, enrollment);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightningId;
    }

    @PostMapping("/lightnings/{lightningId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable Long lightningId, @PathVariable Long enrollmentId) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        lightningService.rejectEnrollment(lightning, enrollment);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightningId;
    }

    @PostMapping("/lightnings/{lightningId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable Long lightningId, @PathVariable Long enrollmentId) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        lightningService.checkInEnrollment(enrollment);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightningId;
    }

    @PostMapping("/lightnings/{lightningId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                          @PathVariable Long lightningId, @PathVariable Long enrollmentId) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        Lightning lightning = lightningRepository.findById(lightningId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        lightningService.cancelCheckInEnrollment(enrollment);
        return "redirect:/gathering/" + gathering.getEncodedPath() + "/lightnings/" + lightning.getId();
    }
}
