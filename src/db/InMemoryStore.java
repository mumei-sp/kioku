package db;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    private static final Map<String, ValueWithExpiration> store;

    static {
        store = new ConcurrentHashMap<>();
    }

    // Wrapper class to store value and its expiration time
    private static class ValueWithExpiration {
        private final Object value;
        private final long expirationTime;

        public ValueWithExpiration(Object value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public Object getValue() {
            return value;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }

    /**
     * Puts a value in the store with a specified expiration time.
     * @param key The key.
     * @param value The value.
     * @param ttl The time-to-live in milliseconds.
     */
    public static void put(String key, Object value, long ttl) {
        long expirationTime = System.currentTimeMillis() + ttl;
        if (ttl < 0 || expirationTime < 0) {
            expirationTime = Long.MAX_VALUE; // Cap to maximum value to prevent overflow
        }
        store.put(key, new ValueWithExpiration(value, expirationTime));
    }

    /**
     * Gets a value from the store.
     * @param key The key.
     * @return The value, or null if the key does not exist or has expired.
     */
    public static Object get(String key) {
        ValueWithExpiration valueWithExpiration = store.get(key);
        if (valueWithExpiration == null) {
            return null;
        }

        // Check if the key has expired
        if (System.currentTimeMillis() > valueWithExpiration.getExpirationTime()) {
            store.remove(key);  // Remove the expired key
            return null;
        }

        return valueWithExpiration.getValue();
    }

    /**
     * Removes a key from the store.
     * @param key The key to remove.
     */
    public static boolean remove(String key) {
        if (!store.containsKey(key)) return false;
        store.remove(key);
        return true;
    }

    /**
     * Periodically cleans up expired keys.
     */
    static {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // Sleep for 10 seconds
                    long currentTime = System.currentTimeMillis();
                    for (String key : store.keySet()) {
                        ValueWithExpiration valueWithExpiration = store.get(key);
                        if (valueWithExpiration != null && currentTime > valueWithExpiration.getExpirationTime()) {
                            store.remove(key);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanupThread.setDaemon(true);  // Set as a daemon thread to not prevent JVM shutdown
        cleanupThread.start();
    }
}
