package ga.fundamental.revolut.persistence;

import com.google.inject.ImplementedBy;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.persistence.impl.AccountDaoImpl;
import lombok.NonNull;

import java.util.List;

@ImplementedBy(AccountDaoImpl.class)
public interface AccountDao {

    /**
     * Persists given account and returns saved entity with assigned id
     *
     * @param account entity to be saved
     * @return persisted entity with generated id
     */
    Account save(@NonNull Account account);

    /**
     * Removes given account from persistence layer
     * @param account entity to be deleted
     */
    void delete(@NonNull Account account);

    /**
     * Update persisted entity with new values
     * @param account entity to be updated
     * @return entity after update
     */
    Account update(@NonNull Account account);

    /**
     * Get all accounts from persistence layer
     * @return list of accounts
     */
    List<Account> findAll();

    /**
     * Get account by id
     * @param id - account id
     * @return persisted account or null if nothing was found
     */
    Account findById(@NonNull Long id);

}
