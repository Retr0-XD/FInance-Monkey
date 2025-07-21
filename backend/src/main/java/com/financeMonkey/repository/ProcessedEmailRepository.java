package com.financeMonkey.repository;

import com.financeMonkey.model.EmailAccount;
import com.financeMonkey.model.ProcessedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessedEmailRepository extends JpaRepository<ProcessedEmail, UUID> {
    List<ProcessedEmail> findByEmailAccount(EmailAccount emailAccount);
    Optional<ProcessedEmail> findByEmailAccountAndEmailMessageId(EmailAccount emailAccount, String emailMessageId);
    boolean existsByEmailAccountAndEmailMessageId(EmailAccount emailAccount, String emailMessageId);
}
