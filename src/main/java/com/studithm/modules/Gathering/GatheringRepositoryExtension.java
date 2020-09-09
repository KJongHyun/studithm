package com.studithm.modules.Gathering;

import com.studithm.modules.account.Account;
import com.studithm.modules.tag.Tag;
import com.studithm.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface GatheringRepositoryExtension {

    Page<Gathering> findByKeyword(String keyword, Pageable pageable);

    List<Gathering> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);

    List<Gathering> findByManager(Account account);

    List<Gathering> findByMember(Account account);
}
