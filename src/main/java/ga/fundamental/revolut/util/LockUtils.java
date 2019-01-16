package ga.fundamental.revolut.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@UtilityClass
public class LockUtils {

    private Map<String, Lock> locks = new ConcurrentReferenceHashMap<>();

    /**
     * Returns an exclusive lock based on an entity class and id
     *
     * @param id    - entity id
     * @param clazz - entity class
     * @return - An exclusive ReentrantLock to synchronize upon. Non-null
     */
    @NonNull
    public static Lock acquireLock(@NonNull Long id, @NonNull Class clazz) {
        return locks.computeIfAbsent(clazz.getSimpleName() + id, key -> new ReentrantLock());
    }
}
