package ga.fundamental.revolut.rest;

import com.despegar.http.client.*;
import com.despegar.sparkjava.test.SparkServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ga.fundamental.revolut.exception.AccountNotFoundException;
import ga.fundamental.revolut.exception.InsufficientFundsException;
import ga.fundamental.revolut.exception.MalformedRequestException;
import ga.fundamental.revolut.model.Account;
import ga.fundamental.revolut.model.ApiError;
import ga.fundamental.revolut.persistence.impl.AccountDaoImpl;
import ga.fundamental.revolut.service.impl.AccountServiceImpl;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EndpointsTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static EntityManager entityManager = Persistence.createEntityManagerFactory("testManager").createEntityManager();

    public static class SparkTestApplication implements SparkApplication {

        public void init() {
            Provider<EntityManager> testManagerProvider = () -> entityManager;
            new SparkEndpoints(new AccountServiceImpl(new AccountDaoImpl(testManagerProvider)), objectMapper);
        }
    }

    @ClassRule
    public static SparkServer<SparkTestApplication> testServer = new SparkServer<>(SparkTestApplication.class, 8080);

    @Test
    public void shouldReturnEmptyList() throws HttpClientException, IOException {
        GetMethod get = testServer.get("/accounts/list", false);
        HttpResponse response = testServer.execute(get);
        assertEquals(200, response.code());
        List<Account> accounts = objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, Account.class));
        assertEquals(0, accounts.size());
    }

    @Test
    public void shouldAddNewAccount() throws HttpClientException {
        PutMethod put = testServer.put("/accounts/create", "{\"amount\":\"1234.56\"}",  false);
        HttpResponse response = testServer.execute(put);
        assertEquals(200, response.code());
        assertEquals("{\"id\":1,\"balance\":1234.56}", new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldReturnMalformedRequestException() throws HttpClientException, JsonProcessingException {
        PutMethod put = testServer.put("/accounts/create", "{\"amo23unt\":\"1234.56\"}",  false); //error in this line
        HttpResponse response = testServer.execute(put);
        assertEquals(400, response.code());
        assertEquals(objectMapper.writeValueAsString(ApiError.of(new MalformedRequestException())), new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldDeleteAccount() throws HttpClientException {
        DeleteMethod del = testServer.delete("/accounts/1", false);
        HttpResponse response = testServer.execute(del);
        assertEquals(200, response.code());
        assertEquals("{\"id\":1,\"balance\":1234.56}", new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldReturnAccountNotFoundException() throws HttpClientException, JsonProcessingException {
        GetMethod get = testServer.get("/accounts/7", false);
        HttpResponse response = testServer.execute(get);
        assertEquals(500, response.code());
        assertEquals(objectMapper.writeValueAsString(ApiError.of(new AccountNotFoundException())), new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldDepositMoneyToAccount() throws HttpClientException {
        PutMethod put = testServer.put("/accounts/create", "{\"amount\":\"100\"}",  false);
        HttpResponse response = testServer.execute(put);
        assertEquals(200, response.code());
        assertEquals("{\"id\":2,\"balance\":100}", new String(response.body(), StandardCharsets.UTF_8));
        PostMethod post = testServer.post("/accounts/deposit",
                "{\n" +
                "\t\"id\" : \"2\",\n" +
                "\t\"amount\" : \"400\"\n" +
                "}", false);
        response = testServer.execute(post);
        assertEquals(200, response.code());
        assertEquals("{\"id\":2,\"balance\":500}", new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldWithdrawMoneyFromAccount() throws HttpClientException {
        PostMethod post = testServer.post("/accounts/withdraw",
                "{\n" +
                        "\t\"id\" : \"2\",\n" +
                        "\t\"amount\" : \"400\"\n" +
                        "}", false);
        HttpResponse response = testServer.execute(post);
        assertEquals(200, response.code());
        assertEquals("{\"id\":2,\"balance\":100}", new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldReturnInsufficientFundsException() throws HttpClientException, JsonProcessingException {
        PostMethod post = testServer.post("/accounts/withdraw",
                "{\n" +
                        "\t\"id\" : \"2\",\n" +
                        "\t\"amount\" : \"400\"\n" +
                        "}", false); //current balance is 100
        HttpResponse response = testServer.execute(post);
        assertEquals(500, response.code());
        assertEquals(objectMapper.writeValueAsString(ApiError.of(new InsufficientFundsException())), new String(response.body(), StandardCharsets.UTF_8));
    }

    @Test
    public void shouldSuccessfullyTransferMoney() throws HttpClientException {
        PutMethod put = testServer.put("/accounts/create", "{\"amount\":\"222.22\"}", false);
        HttpResponse response = testServer.execute(put);
        assertEquals(200, response.code());
        assertEquals("{\"id\":3,\"balance\":222.22}", new String(response.body(), StandardCharsets.UTF_8));

        put = testServer.put("/accounts/create", "{\"amount\":\"333.33\"}", false);
        response = testServer.execute(put);
        assertEquals(200, response.code());
        assertEquals("{\"id\":4,\"balance\":333.33}", new String(response.body(), StandardCharsets.UTF_8));

        PostMethod post = testServer.post("/accounts/transfer",
                "{\n" +
                "\t\"fromId\" : \"3\",\n" +
                "\t\"toId\" : \"4\",\n" +
                "\t\"amount\" : \"100\"\n" +
                "}", false);
        response = testServer.execute(post);
        assertEquals(200, response.code());
        assertEquals("{\"id\":3,\"balance\":122.22}", new String(response.body(), StandardCharsets.UTF_8));
    }
}
