package ga.fundamental.revolut.service;

import com.google.inject.ImplementedBy;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.service.impl.AccountServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@ImplementedBy(AccountServiceImpl.class)
public interface AccountService {

    Account create(BigDecimal amount);

    Account delete(Long id);

    Account get(Long id);

    List<Account> getAll();

    Account deposit(Long id, BigDecimal amount);

    Account withdraw(Long id, BigDecimal amount);

    Account transfer(Long fromId, Long toId, BigDecimal amount);
}
