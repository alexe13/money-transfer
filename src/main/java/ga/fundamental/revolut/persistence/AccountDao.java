package ga.fundamental.revolut.persistence;

import com.google.inject.ImplementedBy;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.persistence.impl.AccountDaoImpl;
import lombok.NonNull;

import java.util.List;

@ImplementedBy(AccountDaoImpl.class)
public interface AccountDao {

    Account save(@NonNull Account account);

    void delete(@NonNull Account account);

    Account update(@NonNull Account account);

    List<Account> findAll();

    Account findById(Long id);

}
