package ga.fundamental.revolut.persistence.impl;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;

/**
 * Class's only purpose is to start Guice PersistenceService
 */
public class PersistServiceStarter {

    @Inject
    PersistServiceStarter(PersistService service) {
        service.start();
    }
}
