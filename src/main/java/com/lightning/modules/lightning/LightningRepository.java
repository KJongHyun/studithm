package com.lightning.modules.lightning;

import com.lightning.modules.gathering.Gathering;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface LightningRepository extends JpaRepository<Lightning, Long> {

    @EntityGraph(value = "Lightning.withEnrollments", type = EntityGraph.EntityGraphType.LOAD)
    List<Lightning> findByGatheringOrderByStartDateTime(Gathering gathering);
}
