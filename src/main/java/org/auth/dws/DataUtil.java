package org.auth.dws;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DataUtil {

    private DataUtil() {
    }

    /**
     * @param size             Number of key-value pairs to generate.
     * @param generatedEntries A map of existing key-value pairs. May be null.
     * @param desiredRange     The maximum number of days the dates can differ if a key is reused.
     * @return A map of generated key-value pairs.
     **/
    //TODO desiredRange should be a read from a config file or from environment variables
    public static Map<String, String> generatePairs(int size, Map<String, String> generatedEntries, int desiredRange) {
        Map<String, String> entries = new HashMap<>();

        // Initialize a Random instance
        Random random = new Random();

        // Define a formatter for LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Generate keys and values for our database
        for (int i = 0; i < size; i++) {
            // Generate a random LocalDate between now and 30 days ago
            LocalDate date = LocalDate.now().minusDays(random.nextInt(30));
            // If generatedEntries are provided and this key already exists, adjust the date to be within 3 days of the original randomly
            if (generatedEntries != null && generatedEntries.containsKey("key" + i) && random.nextBoolean()) {
                LocalDate originalDate = LocalDate.parse(generatedEntries.get("key" + i), formatter);
                long daysBetween = ChronoUnit.DAYS.between(originalDate, date);
                // If the new date is more than 3 days away from the original, adjust it
                if (Math.abs(daysBetween) > desiredRange) {
                    date = originalDate.plusDays(random.nextInt(desiredRange) + 1);
                }
            }
            entries.put("key" + i, date.format(formatter));
        }

        return entries;
    }

    /**
     * Checks whether two dates are within a certain range of each other.
     *
     * @param startDate The first date.
     * @param endDate The second date.
     * @param desiredRange The maximum number of days the two dates can differ.
     * @return True if the dates are within the desired range of each other, false otherwise.
     */
    public static boolean dateWithinRange(LocalDate startDate, LocalDate endDate, int desiredRange) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return Math.abs(daysBetween) <= desiredRange;
    }

    /**
     * Spawns a new thread that populates a ConcurrentHashMap with entries from a Redis database.
     * The keys are used directly, while the values are parsed as LocalDates.
     *
     * @param tableToPopulate The ConcurrentHashMap to populate.
     * @param redisHandler The RedisHandler to use to connect to the Redis database.
     * @return The Thread that has been spawned. It will have been started, but may not have completed yet.
     */
    public static Thread spawnHashTablePopulationThread(ConcurrentHashMap<String, LocalDate> tableToPopulate, RedisHandler redisHandler) {
        return new Thread(() -> {
            var resource = redisHandler.getJedis();
            resource.keys("*").forEach(key -> {
                String value = resource.type(key);
                if ("string".equals(value)) {
                    tableToPopulate.put(key, LocalDate.parse(resource.get(key)));
                }
            });
        });
    }
}
