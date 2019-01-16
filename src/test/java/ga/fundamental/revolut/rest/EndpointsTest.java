package ga.fundamental.revolut.rest;

import com.despegar.http.client.GetMethod;
import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.sparkjava.test.SparkServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import ga.fundamental.revolut.persistence.impl.AccountDaoImpl;
import ga.fundamental.revolut.persistence.impl.PersistServiceStarter;
import ga.fundamental.revolut.service.impl.AccountServiceImpl;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class EndpointsTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static EntityManager entityManager = Persistence.createEntityManagerFactory("testManager").createEntityManager();

    public static class SparkTestApplication implements SparkApplication {

        public void init() {
            Provider<EntityManager> testManagerProvider = () -> entityManager;
//            new SparkEndpoints(new AccountServiceImpl(new AccountDaoImpl(Persistence.createEntityManagerFactory("testManager").createEntityManager())), objectMapper);
        }
    }

    @ClassRule
    public static SparkServer<SparkTestApplication> testServer = new SparkServer<>(SparkTestApplication.class, 8080);

    @Test
    public void test() throws HttpClientException, InterruptedException {
        GetMethod get = testServer.get("/accounts/list", false);
        HttpResponse response = testServer.execute(get);
        assertEquals(200, response.code());

        IntStream.rangeClosed(0, 40).
                forEach(i -> new Thread(() -> {
                    try {
                        testServer.execute(testServer.put("/accounts/create",
                                "{" +
                                        "\n" +
                                        " \"amount\" : \"500\"\n" +
                                        "}", false));
                    } catch (HttpClientException e) {
                        e.printStackTrace();
                    }
                }, "Thread" + i).start());
        System.out.println(new String(testServer.execute(get).body(), StandardCharsets.UTF_8));
    }
}
