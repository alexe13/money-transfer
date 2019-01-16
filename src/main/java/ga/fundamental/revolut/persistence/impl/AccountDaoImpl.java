package ga.fundamental.revolut.persistence.impl;

import com.google.inject.persist.Transactional;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.persistence.AccountDao;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Slf4j
/**
 * Implementation of {@link AccountDao]
 * Transactions are managed by guice-persist
 */
public class AccountDaoImpl implements AccountDao {

    @Getter
    private Provider<EntityManager> entityManagerProvider;

    @Inject
    public AccountDaoImpl(Provider<EntityManager> entityManagerProvider) {
        this.entityManagerProvider = entityManagerProvider;
    }

    @Override
    @Transactional
    public Account save(@NonNull Account account) {
        entityManagerProvider.get().persist(account);
        return account;
    }

    @Override
    @Transactional
    public void delete(@NonNull Account account) {
        EntityManager manager = entityManagerProvider.get();
        manager.remove(manager.contains(account) ? account : manager.merge(account));
    }

    @Override
    @Transactional
    public Account update(Account account) {
        account = entityManagerProvider.get().merge(account);
        return account;
    }

    @Override
    @Transactional
    public List<Account> findAll() {
        EntityManager manager = entityManagerProvider.get();
        CriteriaQuery<Account> criteria = manager.getCriteriaBuilder().createQuery(Account.class);
        Root<Account> root = criteria.from(Account.class);
        criteria.select(root);
        return manager.createQuery(criteria).getResultList();
    }

    @Override
    @Transactional
    public Account findById(Long id) {
        return entityManagerProvider.get().find(Account.class, id);
    }

//    private void wrapInTransaction(Runnable action) {
//        try {
//            entityManager.getTransaction().begin();
//            action.run();
//            entityManager.getTransaction().commit();
//        } catch (Exception ex) {
//            log.error("Transaction failed", ex);
//            entityManager.getTransaction().rollback();
//        }
//    }
//
//    private Account wrapInTransaction(Callable<Account> action) {
//        try {
//            entityManager.getTransaction().begin();
//            Account accountFromDb = action.call();
//            entityManager.getTransaction().commit();
//            return accountFromDb;
//        } catch (Exception ex) {
//            log.error("Transaction failed", ex);
//            entityManager.getTransaction().rollback();
//            return null;
//        }
//    }
}
