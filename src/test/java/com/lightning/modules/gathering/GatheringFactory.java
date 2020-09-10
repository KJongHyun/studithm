package com.lightning.modules.gathering;

import com.lightning.modules.account.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringFactory {

    private final GatheringService gatheringService;

    public Gathering creatGathering(String path, Account manager) {
        Gathering newGathering = new Gathering();
        newGathering.setPath(path);
        gatheringService.createNewGathering(newGathering, manager);
        return newGathering;
    }

}
