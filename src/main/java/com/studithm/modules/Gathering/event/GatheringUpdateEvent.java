package com.studithm.modules.Gathering.event;

import com.studithm.modules.Gathering.Gathering;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GatheringUpdateEvent {

    private final Gathering gathering;
    private final String message;


}
