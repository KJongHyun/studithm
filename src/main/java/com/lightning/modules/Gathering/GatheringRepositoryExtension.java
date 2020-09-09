package com.lightning.modules.Gathering;

import com.lightning.modules.account.Account;
import com.lightning.modules.tag.Tag;
import com.lightning.modules.zone.Zone;
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
