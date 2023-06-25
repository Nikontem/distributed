package org.auth.dws;

import org.auth.dws.model.JoinedResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JoinAlgorithms {

    final RedisHandler firstRedisHandler;
    final RedisHandler secondRedisHandler;


    public JoinAlgorithms(RedisHandler firstRedisHandler, RedisHandler secondRedisHandler) {
        this.firstRedisHandler = firstRedisHandler;
        this.secondRedisHandler = secondRedisHandler;
    }


    public ArrayList<JoinedResult> pipelinedHashJoin(int desiredRange) {
        System.out.println("Starting pipelined hash join");
        var result = new ArrayList<JoinedResult>();
        long startTime = System.currentTimeMillis();
        var smallHashMap = new ConcurrentHashMap<String, LocalDate>();

        //Set up logic to create hashtables for both retrieved datasets
        Thread smallHashTableThread = DataUtil.spawnHashTablePopulationThread(smallHashMap, firstRedisHandler);

        // Start the threads
        smallHashTableThread.start();

        // Wait for the threads to finish
        try {
            smallHashTableThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
         Iterate over the big dataset and if the key exists in the small dataset and their datediff is smaller than the desired range
            add the result to the result list
         */
        var resource = secondRedisHandler.getJedis();
        resource.keys("*")
                .stream()
                .filter(smallHashMap::containsKey)
                .filter(key -> DataUtil.dateWithinRange(smallHashMap.get(key), LocalDate.parse(resource.get(key)), desiredRange))
                .forEach(key -> result.add(new JoinedResult(key, smallHashMap.get(key), LocalDate.parse(resource.get(key)))));
        var endTime = System.currentTimeMillis();

        //Print execution time in seconds
        System.out.println("Pipelined hash join took " + (endTime - startTime) / 1000.0 + " seconds");
        return result;
    }


    public ArrayList<JoinedResult> semiJoin(int desiredRange) {
        System.out.println("Starting Semi join");
        var result = new ArrayList<JoinedResult>();
        var startTime = System.currentTimeMillis();
        Set<String> smallKeyset;
        Set<String> bigKeySet;

        var firstKeyset = firstRedisHandler.getJedis().keys("*");
        var secondKeySet = secondRedisHandler.getJedis().keys("*");

        if (firstKeyset.size() < secondKeySet.size()) {
            smallKeyset = firstKeyset;
            bigKeySet = secondKeySet;
        } else {
            smallKeyset = secondKeySet;
            bigKeySet = firstKeyset;
        }

        //Open resource once for each RedisHandler, otherwise redis client will hang
        var firstResource = firstRedisHandler.getJedis();
        var secondResource = secondRedisHandler.getJedis();

        result = smallKeyset.stream()
                .filter(bigKeySet::contains)
                .filter(key -> {
                    LocalDate smallDate = LocalDate.parse(firstResource.get(key));
                    LocalDate bigDate = LocalDate.parse(secondResource.get(key));
                    return DataUtil.dateWithinRange(smallDate, bigDate, 3);
                })
                .map(key -> new JoinedResult(key, LocalDate.parse(firstResource.get(key)), LocalDate.parse(secondResource.get(key))))
                .collect(Collectors.toCollection(ArrayList::new));

        var endTime = System.currentTimeMillis();

        System.out.println("Semi join took " + (endTime - startTime) / 1000.0 + " seconds");

        return result;
    }

}
