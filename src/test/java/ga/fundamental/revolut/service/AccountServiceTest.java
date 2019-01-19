package ga.fundamental.revolut.service;

import ga.fundamental.revolut.exception.InsufficientFundsException;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.persistence.impl.AccountDaoImpl;
import ga.fundamental.revolut.service.impl.AccountServiceImpl;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class AccountServiceTest {

    private static EntityManager entityManager = Persistence.createEntityManagerFactory("testManager").createEntityManager();
    private AccountService accountService = new AccountServiceImpl(new AccountDaoImpl(() -> entityManager));


    @Test
    public void testConcurrentDeposit() throws InterruptedException {
        Long id = accountService.create(BigDecimal.ZERO).getId();
        //test that no double-deposits are possible in multithreaded environment
        CountDownLatch latch = new CountDownLatch(10);
        IntStream.range(0, 10)
                .forEach(i -> new Thread(() -> {
                    accountService.deposit(id, BigDecimal.valueOf(100));
                    latch.countDown();
                }, "Thread" + i).start());
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(BigDecimal.valueOf(1000), accountService.get(id).getBalance());
    }

    @Test
    public void testConcurrentWithdraw() throws InterruptedException {
        Long id = accountService.create(BigDecimal.ZERO).getId();
        accountService.deposit(id, BigDecimal.valueOf(700));
        //test that account balance can not be set to negative value in a multithreaded environment
        CountDownLatch latch = new CountDownLatch(10);
        IntStream.range(0, 10)
                .forEach(i -> new Thread(() -> {
                    try {
                        accountService.withdraw(id, BigDecimal.valueOf(100));
                    } catch (InsufficientFundsException ignored) {
                    } finally {
                        latch.countDown();
                    }
                }, "Thread" + i).start());
        latch.await(10, TimeUnit.SECONDS);
        assertEquals(BigDecimal.ZERO, accountService.get(id).getBalance());
    }

    @Test
    public void testConcurrentTransfer() throws InterruptedException {
        //test that no deadlocks are happening during different-order transfers
        Long idFrom = accountService.create(BigDecimal.valueOf(500)).getId();
        Long idTo = accountService.create(BigDecimal.valueOf(500)).getId();
        ExecutorService es = Executors.newFixedThreadPool(10);
        Callable<Account> oneToTwo = () -> accountService.transfer(idFrom, idTo, BigDecimal.valueOf(100));
        Callable<Account> twoToOne = () -> accountService.transfer(idTo, idFrom, BigDecimal.valueOf(100));

        IntStream.range(0, 10)
                .mapToObj(i -> i % 2 == 0 ? oneToTwo : twoToOne)
                .map(es::submit)
                .forEach(f -> {
                    try {
                        f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);

        //account balances should remain unchanged
        assertEquals(BigDecimal.valueOf(500), accountService.get(idFrom).getBalance());
        assertEquals(BigDecimal.valueOf(500), accountService.get(idTo).getBalance());
    }
}
