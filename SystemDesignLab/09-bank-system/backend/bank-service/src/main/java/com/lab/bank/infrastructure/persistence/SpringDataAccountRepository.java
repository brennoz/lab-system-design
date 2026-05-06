package com.lab.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {

    // Algorithm: optimistic locking — WHERE version = :version ensures no concurrent modification
    @Modifying
    @Transactional
    @Query("UPDATE AccountJpaEntity a SET a.balance = :balance, a.version = a.version + 1 WHERE a.id = :id AND a.version = :version")
    int updateBalanceWithVersion(@Param("id") UUID id,
                                 @Param("balance") BigDecimal balance,
                                 @Param("version") long version);
}
