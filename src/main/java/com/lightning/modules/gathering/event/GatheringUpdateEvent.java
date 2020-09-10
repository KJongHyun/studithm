package com.lightning.modules.gathering.event;

import com.lightning.modules.gathering.Gathering;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GatheringUpdateEvent {

    private final Gathering gathering;
    private final String message;


}
