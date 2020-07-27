package com.studithm.modules.account;

import com.querydsl.core.types.Predicate;
import com.studithm.modules.study.QStudy;
import com.studithm.modules.tag.Tag;
import com.studithm.modules.zone.Zone;

import java.util.Set;


public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }

}
