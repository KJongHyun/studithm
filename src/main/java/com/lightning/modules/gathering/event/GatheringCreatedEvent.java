package com.lightning.modules.gathering.event;

import com.lightning.modules.gathering.Gathering;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GatheringCreatedEvent {

    private final Gathering gathering;

}
