package com.studithm.modules.study;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.studithm.modules.account.Account;
import com.studithm.modules.tag.QTag;
import com.studithm.modules.tag.Tag;
import com.studithm.modules.zone.QZone;
import com.studithm.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Set;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension {

    public StudyRepositoryExtensionImpl() {
        super(Study.class);
    }

    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .distinct();
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    @Override
    public List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.closed.isFalse())
                .and(study.tags.any().in(tags))
                .and(study.zones.any().in(zones)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .orderBy(study.publishedDateTime.desc())
                .distinct()
                .limit(9);

        return query.fetch();
    }

    @Override
    public List<Study> findByManager(Account account) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.managers.any().eq(account)).limit(5);

        return query.fetch();
    }

    @Override
    public List<Study> findByMember(Account account) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.members.any().eq(account)).limit(5);

        return query.fetch();
    }

}
