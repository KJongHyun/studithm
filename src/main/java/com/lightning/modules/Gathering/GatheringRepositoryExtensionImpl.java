package com.lightning.modules.Gathering;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.lightning.modules.account.Account;
import com.lightning.modules.tag.QTag;
import com.lightning.modules.tag.Tag;
import com.lightning.modules.zone.QZone;
import com.lightning.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class GatheringRepositoryExtensionImpl extends QuerydslRepositorySupport implements GatheringRepositoryExtension {

    public GatheringRepositoryExtensionImpl() {
        super(Gathering.class);
    }

    @Override
    public Page<Gathering> findByKeyword(String keyword, Pageable pageable) {
        QGathering gathering = QGathering.gathering;
        JPQLQuery<Gathering> query = from(gathering).where(gathering.published.isTrue()
                .and(gathering.title.containsIgnoreCase(keyword))
                .or(gathering.tags.any().title.containsIgnoreCase(keyword))
                .or(gathering.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(gathering.tags, QTag.tag).fetchJoin()
                .leftJoin(gathering.zones, QZone.zone).fetchJoin()
                .distinct();
        JPQLQuery<Gathering> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Gathering> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Gathering> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QGathering gathering = QGathering.gathering;
        JPQLQuery<Gathering> query = from(gathering).where(gathering.published.isTrue()
                .and(gathering.closed.isFalse())
                .and(gathering.tags.any().in(tags))
                .and(gathering.zones.any().in(zones)))
                .leftJoin(gathering.tags, QTag.tag).fetchJoin()
                .leftJoin(gathering.zones, QZone.zone).fetchJoin()
                .orderBy(gathering.publishedDateTime.desc())
                .distinct()
                .limit(9);

        return query.fetch();
    }

    @Override
    public List<Gathering> findByManager(Account account) {
        QGathering gathering = QGathering.gathering;
        JPQLQuery<Gathering> query = from(gathering).where(gathering.managers.any().eq(account)).limit(5);

        return query.fetch();
    }

    @Override
    public List<Gathering> findByMember(Account account) {
        QGathering gathering = QGathering.gathering;
        JPQLQuery<Gathering> query = from(gathering).where(gathering.members.any().eq(account)).limit(5);

        return query.fetch();
    }

}
