package com.lightning.modules.Gathering;

import com.lightning.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryExtension {
    boolean existsByPath(String path);

    // attributeNode로 선언된것들은 FetchType.EAGER 나머지는 default
    @EntityGraph(value = "Gathering.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Gathering findByPath(String path);

    // attributeNode로 선언된것들은 FetchType.EAGER 나머지는 LAZY
    @EntityGraph(value = "Gathering.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Gathering findGatheringWithTagsByPath(String path);

    @EntityGraph(value = "Gathering.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Gathering findGatheringWithZonesByPath(String path);

    @EntityGraph(value = "Gathering.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Gathering findGatheringWithManagersByPath(String path);

    @EntityGraph(value = "Gathering.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Gathering findGatheringWithMembersByPath(String path);

    Gathering findGatheringOnlyByPath(String path);

    @EntityGraph(value = "Gathering.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    Gathering findGatheringWithTagsAndZonesById(Long id);

    @EntityGraph(attributePaths = {"managers", "members"})
    Gathering findGatheringWithManagersAndMembersById(Long id);

    @EntityGraph(attributePaths = {"tags", "zones"})
    List<Gathering> findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Gathering> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Gathering> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}