package com.studithm.modules.study;

import com.studithm.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {
    boolean existsByPath(String path);

    // attributeNode로 선언된것들은 FetchType.EAGER 나머지는 default
    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    // attributeNode로 선언된것들은 FetchType.EAGER 나머지는 LAZY
    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers", "members"})
    Study findStudyWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"tags", "zones"})
    List<Study> findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}