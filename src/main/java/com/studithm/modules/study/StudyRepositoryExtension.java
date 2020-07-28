package com.studithm.modules.study;

import com.studithm.modules.account.Account;
import com.studithm.modules.tag.Tag;
import com.studithm.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);

    List<Study> findByManager(Account account);

    List<Study> findByMember(Account account);
}
