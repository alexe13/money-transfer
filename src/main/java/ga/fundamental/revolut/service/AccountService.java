package ga.fundamental.revolut.service;

import com.google.inject.ImplementedBy;
import ga.fundamental.revolut.exception.AccountNotFoundException;
import ga.fundamental.revolut.exception.InsufficientFundsException;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.service.impl.AccountServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@ImplementedBy(AccountServiceImpl.class)
public interface AccountService {

    /**
     * Creates new account, persisting it and generating an id
     * @param amount - initial balance of account to be created
     * @return - created account
     */
    Account create(BigDecimal amount);

    /**
     * Deletes account with given id
     * @param id - id of account to be deleted
     * @return deleted account
     * @throws AccountNotFoundException if account with given id does not exist
     */
    Account delete(Long id);

    /**
     * Retrieves account with given id
     * @param id - id of account to be found
     * @return account entity
     * @throws AccountNotFoundException if account with given id does not exist
     */
    Account get(Long id);

    /**
     * Get all existing accounts
     * @return - a list of account entities
     */
    List<Account> getAll();

    /**
     * Deposits a given amount of currency to an account with given id
     * @param id - id of account to be deposited
     * @param amount - amount to deposit
     * @return account with updated balance
     * @throws AccountNotFoundException if account with given id does not exist
     */
    Account deposit(Long id, BigDecimal amount);

    /**
     * Withdraws a given amount of currency from an account with given id
     * @param id - id of account to be withdrawn
     * @param amount - amount to withdraw
     * @return account with updated balance
     * @throws AccountNotFoundException if account with given id does not exist
     * @throws InsufficientFundsException if account balance is less than withdraw amount
     */
    Account withdraw(Long id, BigDecimal amount);

    /**
     * Transfers given amount of currency from one account to another
     * @param fromId - id of account to withdraw
     * @param toId - id of account to deposit
     * @param amount - amount to be transferred
     * @return - account that was withdrawn
     * @throws AccountNotFoundException if one or both accounts with given id does not exist
     * @throws InsufficientFundsException if withdraw account balance is less than transfer amount
     */
    Account transfer(Long fromId, Long toId, BigDecimal amount);
}
