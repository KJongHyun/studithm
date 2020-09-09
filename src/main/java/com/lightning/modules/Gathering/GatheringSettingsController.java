package com.lightning.modules.Gathering;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightning.modules.account.Account;
import com.lightning.modules.account.CurrentAccount;
import com.lightning.modules.tag.TagForm;
import com.lightning.modules.zone.ZoneForm;
import com.lightning.modules.Gathering.form.GatheringDescriptionForm;
import com.lightning.modules.tag.Tag;

import com.lightning.modules.tag.TagRepository;
import com.lightning.modules.tag.TagService;
import com.lightning.modules.zone.Zone;
import com.lightning.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gathering/{path}/settings")
@RequiredArgsConstructor
public class GatheringSettingsController {

    private final GatheringService gatheringService;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    @GetMapping("/description")
    public String viewGatheringSetting(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        model.addAttribute(modelMapper.map(gathering, GatheringDescriptionForm.class));
        return "gathering/settings/description";
    }

    @PostMapping("/description")
    public String updateGatheringInfo(@CurrentAccount Account account, @PathVariable String path,
                                  @Valid GatheringDescriptionForm gatheringDescriptionForm, Errors errors,
                                  Model model, RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);

        if (errors.hasErrors()) {
            // 폼에서 받았던 데이터와 에러는 model이 알아서 담아줌 다시 넣을 필요 없음
            model.addAttribute(account);
            model.addAttribute(gathering);

            return "gathering/settings/description";
        }

        gatheringService.updateGatheringDescription(gathering, gatheringDescriptionForm);
        attributes.addFlashAttribute("message", "모임 소개를 수정했습니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/description";
    }

    @GetMapping("/banner")
    public String viewGatheringBannerSetting(@CurrentAccount Account account, @PathVariable String path,
                                         Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        return "gathering/settings/banner";
    }

    @PostMapping("/banner")
    public String updateGatheringBanner(@CurrentAccount Account account, @PathVariable String path,
                                    String image, RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        gatheringService.updateImage(gathering, image);
        attributes.addFlashAttribute("message", "모임 이미지를 수정했습니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/enable")
    public String enableGatheringBanner(@CurrentAccount Account account, @PathVariable String path) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        gatheringService.enableGatheringBanner(gathering);
        return "redirect:/gathering/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableGatheringBanner(@CurrentAccount Account account, @PathVariable String path) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        gatheringService.disableGatheringBanner(gathering);
        return "redirect:/gathering/" + getPath(path) + "/settings/banner";
    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/tags")
    public String updateGatheringTags(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        model.addAttribute("tags", gathering.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "gathering/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        String title = tagForm.getTagTitle();
        Tag tag = tagService.findOrCreateNew(title);
        gatheringService.addTag(gathering, tag);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        gatheringService.removeTag(gathering, tag);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String updateGatheringZones(@CurrentAccount Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        model.addAttribute("zones", gathering.getZones().stream().map(Zone::toString).collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "gathering/settings/zones";
    }

    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        Gathering gathering = gatheringService.getGatheringToUpdateZone(account, path);
        gatheringService.addZone(gathering, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @PathVariable String path, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }
        Gathering gathering = gatheringService.getGatheringToUpdateTag(account, path);
        gatheringService.removeZone(gathering, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/gathering")
    public String gatheringSettingForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(gathering);
        return "gathering/settings/gathering";
    }

    @PostMapping("/gathering/publish")
    public String publishGathering(@CurrentAccount Account account, @PathVariable String path,
                               RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        gatheringService.publish(gathering);
        attributes.addFlashAttribute("message", "모임을 공개했습니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
    }

    @PostMapping("/gathering/close")
    public String closeGathering(@CurrentAccount Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        gatheringService.close(gathering);
        attributes.addFlashAttribute("message", "모임을 종료했습니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        if (!gathering.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
        }

        gatheringService.startRecruit(gathering);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentAccount Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdate(account, path);
        if (!gathering.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
        }

        gatheringService.stopRecruit(gathering);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
    }

    @PostMapping("/gathering/path")
    public String updateGatheringPath(@CurrentAccount Account account, @PathVariable String path, String newPath,
                                  Model model,RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        if (!gatheringService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(gathering);
            model.addAttribute("gatheringPathError", "해당 모임 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
        }

        gatheringService.updateGatheringPath(gathering, newPath);
        attributes.addFlashAttribute("message", "모임 경로를 수정했습니다.");
        return "redirect:/gathering/" + getPath(newPath) + "/settings/gathering";
    }

    @PostMapping("/gathering/title")
    public String updateGatheringTitle(@CurrentAccount Account account, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        if (!gatheringService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(gathering);
            model.addAttribute("gatheringTitleError", "모임 이름을 다시 입력하세요.");
            return "gathering/settings/gathering";
        }

        gatheringService.updateGatheringTitle(gathering, newTitle);
        attributes.addFlashAttribute("message", "모임 이름을 수정했습니다.");
        return "redirect:/gathering/" + getPath(path) + "/settings/gathering";
    }

    @PostMapping("/gathering/remove")
    public String removeGathering(@CurrentAccount Account account, @PathVariable String path) {
        Gathering gathering = gatheringService.getGatheringToUpdateStatus(account, path);
        gatheringService.remove(gathering);
        return "redirect:/";
    }
}
