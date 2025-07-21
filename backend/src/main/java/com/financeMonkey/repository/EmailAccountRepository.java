package com.financeMonkey.repository;

import com.financeMonkey.model.EmailAccount;
import com.financeMonkey.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailAccountRepository extends JpaRepository<EmailAccount, UUID> {
    List<EmailAccount> findByUser(User user);
    List<EmailAccount> findByUserAndSyncStatus(User user, EmailAccount.SyncStatus status);
    Optional<EmailAccount> findByUserAndEmailAddress(User user, String emailAddress);
}
