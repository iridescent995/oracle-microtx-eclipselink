package com.microtx.sagaorchservice.repository;

import com.microtx.sagaorchservice.dto.Account;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AccountRepository {
    private static final Logger LOG = LoggerFactory.getLogger(AccountRepository.class);

    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManager entityManager;

    @Transactional
    public Account findByAccountId(String accountId) {
        Query query = entityManager.createNativeQuery("SELECT * FROM accounts where accountid= ?", Account.class);
        query.setParameter(1, accountId);
        Account account = (Account) query.getSingleResult();
        return account;
    }

    @Transactional
    public Account merge(Account account) {
        LOG.info("\n\n: merge Account" + account.getAccountId());
        entityManager.merge(account);
        LOG.info("Merged success: " + account.getAmount());
        return account;
    }

    @Transactional
    public void flush() {
        LOG.info("\n\n: flush Account" );
        entityManager.flush();
    }
}