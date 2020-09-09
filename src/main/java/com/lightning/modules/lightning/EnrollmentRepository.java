package com.lightning.modules.lightning;

import com.lightning.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByLightningAndAccount(Lightning lightning, Account account);

    Enrollment findByLightningAndAccount(Lightning lightning, Account account);

    @EntityGraph("Enrollment.withLightningAndGathering")
    List<Enrollment> findByAccountAndAcceptedOrderByEnrolledAt(Account loginAccount, boolean accepted);
}
