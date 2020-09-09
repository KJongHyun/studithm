package com.studithm.modules.Gathering;

import com.studithm.modules.Gathering.form.GatheringDescriptionForm;
import com.studithm.modules.account.Account;
import com.studithm.modules.Gathering.event.GatheringCreatedEvent;
import com.studithm.modules.Gathering.event.GatheringUpdateEvent;
import com.studithm.modules.Gathering.form.GatheringForm;
import com.studithm.modules.tag.Tag;
import com.studithm.modules.tag.TagRepository;
import com.studithm.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Transactional
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    private final TagRepository tagRepository;

    public Gathering createNewGathering(Gathering gathering, Account account) {

        Gathering newGathering = gatheringRepository.save(gathering);
        newGathering.addManager(account);
        return newGathering;
    }

    public Gathering getGatheringToUpdate(Account account, String path) {
        Gathering gathering = this.getGathering(path);
        checkIfManager(account, gathering);

        return gathering;
    }

    public Gathering getGatheringToUpdateTag(Account account, String path) {
        Gathering gathering = gatheringRepository.findGatheringWithTagsByPath(path);
        checkIfExistingGathering(path, gathering);
        checkIfManager(account, gathering);

        return gathering;
    }

    public Gathering getGatheringToUpdateZone(Account account, String path) {
        Gathering gathering = gatheringRepository.findGatheringWithZonesByPath(path);
        checkIfExistingGathering(path, gathering);
        checkIfManager(account, gathering);

        return gathering;
    }

    public Gathering getGatheringToUpdateStatus(Account account, String path) {
        Gathering gathering = gatheringRepository.findGatheringWithManagersByPath(path);
        checkIfExistingGathering(path, gathering);
        checkIfManager(account, gathering);
        return gathering;
    }

    public Gathering getGathering(String path) {
        Gathering gathering = this.gatheringRepository.findByPath(path);
        checkIfExistingGathering(path, gathering);

        return gathering;
    }

    public void updateGatheringDescription(Gathering gathering, GatheringDescriptionForm gatheringDescriptionForm) {
        modelMapper.map(gatheringDescriptionForm, gathering);
        eventPublisher.publishEvent(new GatheringUpdateEvent(gathering, "스터디 소개를 수정했습니다."));
    }

    public void updateImage(Gathering gathering, String image) {
        gathering.setImage(image);
    }

    public void enableGatheringBanner(Gathering gathering) {
        gathering.setUseBanner(true);
    }

    public void disableGatheringBanner(Gathering gathering) {
        gathering.setUseBanner(false);
    }

    public void addTag(Gathering gathering, Tag tag) {
        gathering.getTags().add(tag);
    }

    public void removeTag(Gathering gathering, Tag tag) {
        gathering.getTags().remove(tag);
    }

    public void addZone(Gathering gathering, Zone zone) {
        gathering.getZones().add(zone);
    }

    public void removeZone(Gathering gathering, Zone zone) {
        gathering.getZones().remove(zone);
    }

    private void checkIfManager(Account account, Gathering gathering) {
        if (!gathering.isManagedBy(account)) {
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkIfExistingGathering(String path, Gathering gathering) {
        if (gathering == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void publish(Gathering gathering) {
        gathering.publish();
        this.eventPublisher.publishEvent(new GatheringCreatedEvent(gathering));
    }

    public void close(Gathering gathering) {
        gathering.close();
        eventPublisher.publishEvent(new GatheringUpdateEvent(gathering, "스터디를 종료했습니다."));
    }

    public void startRecruit(Gathering gathering) {
        gathering.startRecruit();
        eventPublisher.publishEvent(new GatheringUpdateEvent(gathering, "팀원 모집을 시작합니다."));
    }

    public void stopRecruit(Gathering gathering) {
        gathering.stopRecruit();
        eventPublisher.publishEvent(new GatheringUpdateEvent(gathering, "팀원 모집을 중단했습니다."));
    }

    public boolean isValidPath(String newPath) {
        if (!newPath.matches(GatheringForm.VALID_PATH_PATTERN)) {
            return false;
        }
        return !gatheringRepository.existsByPath(newPath);
    }

    public void updateGatheringPath(Gathering gathering, String newPath) {
        gathering.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateGatheringTitle(Gathering gathering, String newTitle) {
        gathering.setTitle(newTitle);
    }

    public void remove(Gathering gathering) {
        if (gathering.isRemovable()) {
            gatheringRepository.delete(gathering);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Gathering gathering, Account account) {
        gathering.addMember(account);
    }

    public void removeMember(Gathering gathering, Account account) {
        gathering.removeMember(account);
    }

    public Gathering getGatheringToEnroll(String path) {
        Gathering gathering = gatheringRepository.findGatheringOnlyByPath(path);
        checkIfExistingGathering(path, gathering);
        return gathering;
    }

    public void generateTestGatherings(Account account) {
        for (int i = 0; i < 30; i++) {
            String randomValue = RandomString.make(5);
            Gathering gathering = Gathering.builder()
                    .title("테스트 스터디 " + randomValue)
                    .path("test-" + randomValue)
                    .shortDescription("테스트용 스터디 입니다.")
                    .fullDescription("test")
                    .tags(new HashSet<>())
                    .managers(new HashSet<>())
                    .build();
            gathering.publish();
            Gathering newGathering = this.createNewGathering(gathering, account);
            Tag jpa = tagRepository.findByTitle("jpa");
            newGathering.getTags().add(jpa);
        }
    }
}
