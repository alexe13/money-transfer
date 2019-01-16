package ga.fundamental.revolut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.persist.jpa.JpaPersistModule;
import ga.fundamental.revolut.persistence.AccountDao;
import ga.fundamental.revolut.persistence.impl.PersistServiceStarter;
import ga.fundamental.revolut.rest.SparkEndpoints;
import spark.Spark;

public class Application {

    public static void main(String[] args) {
        initialize();
    }

    private static void initialize() {
        Injector injector = Guice.createInjector(new JpaPersistModule("entityManager"), new AbstractModule() {
            @Override
            protected void configure() {

            }

            @Provides
            ObjectMapper objectMapper() {
                return new ObjectMapper();
            }
        });

        injector.getInstance(PersistServiceStarter.class);
        injector.getInstance(AccountDao.class);
        injector.getInstance(SparkEndpoints.class);

        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));
    }

}
