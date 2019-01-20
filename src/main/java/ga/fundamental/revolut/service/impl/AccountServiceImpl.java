package ga.fundamental.revolut.service.impl;

import ga.fundamental.revolut.exception.AccountNotFoundException;
import ga.fundamental.revolut.exception.InsufficientFundsException;
import ga.fundamental.revolut.exception.MalformedRequestException;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.persistence.AccountDao;
import ga.fundamental.revolut.service.AccountService;
import ga.fundamental.revolut.util.LockUtils;
import lombok.NonNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

public class AccountServiceImpl implements AccountService {

    private AccountDao accountDao;

    @Inject
    public AccountServiceImpl(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public Account create(@NonNull BigDecimal amount) {
        Account account = new Account();
        account.setBalance(amount);
        return accountDao.save(account);
    }

    @Override
    public Account delete(@NonNull Long id) {
        Account accToBeDeleted = get(id);
        accountDao.delete(accToBeDeleted);
        return accToBeDeleted;
    }

    @Override
    public Account get(@NonNull Long id) {
        return Optional.ofNullable(accountDao.findById(id)).orElseThrow(AccountNotFoundException::new);
    }

    @Override
    public List<Account> getAll() {
        return accountDao.findAll();
    }

    @Override
    public Account deposit(@NonNull Long id, @NonNull BigDecimal amount) {
        Lock lock = LockUtils.acquireLock(id, Account.class);
        lock.lock();
        Account accountToDeposit;
        try {
            accountToDeposit = Optional.ofNullable(accountDao.findById(id)).orElseThrow(AccountNotFoundException::new);

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new MalformedRequestException("Negative amount");
            }

            accountToDeposit.setBalance(accountToDeposit.getBalance().add(amount));
            accountDao.update(accountToDeposit);
        } finally {
            lock.unlock();
        }

        return accountToDeposit;
    }

    @Override
    public Account withdraw(@NonNull Long id, @NonNull BigDecimal amount) {
        Lock lock = LockUtils.acquireLock(id, Account.class);
        lock.lock();
        Account accountToWithdraw;
        try {
            accountToWithdraw = Optional.ofNullable(accountDao.findById(id)).orElseThrow(AccountNotFoundException::new);

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new MalformedRequestException("Negative amount");
            }

            if (accountToWithdraw.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Current balance of " + accountToWithdraw.getBalance() + " is less then withdraw amount");
            }

            accountToWithdraw.setBalance(accountToWithdraw.getBalance().subtract(amount));
            accountDao.update(accountToWithdraw);
        } finally {
            lock.unlock();
        }

        return accountToWithdraw;
    }

    @Override
    public Account transfer(@NonNull Long fromId, @NonNull Long toId, @NonNull BigDecimal amount) {

        //acquire locks in a predictable way to avoid lock-ordering deadlocks
        Lock firstLock = LockUtils.acquireLock(Math.min(fromId, toId), Account.class);
        Lock secondLock = LockUtils.acquireLock(Math.max(fromId, toId), Account.class);

        firstLock.lock();
        secondLock.lock();

        Account accountToWithdraw;
        Account accountToDeposit;
        try {
            accountToWithdraw = Optional.ofNullable(accountDao.findById(fromId)).orElseThrow(AccountNotFoundException::new);
            accountToDeposit = Optional.ofNullable(accountDao.findById(toId)).orElseThrow(AccountNotFoundException::new);

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new MalformedRequestException("Negative amount");
            }

            if (accountToWithdraw.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Current balance of " + accountToWithdraw.getBalance() + " is less then withdraw amount");
            }

            accountToWithdraw.setBalance(accountToWithdraw.getBalance().subtract(amount));
            accountToDeposit.setBalance(accountToDeposit.getBalance().add(amount));
            accountDao.update(accountToWithdraw);
            accountDao.update(accountToDeposit);
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }

        return accountToWithdraw;
    }
}
