package org.auth.dws;

import java.util.Map;

public class Main {
    public static void main(String[] args) {

        //Initialize two RedisHandler instances
        var firstRedisHandler = new RedisHandler("redis1", 6379);
        var secondRedisHandler = new RedisHandler("redis2", 6378);

        // Populate the Redis instances with data'
        populateDatabase(firstRedisHandler, secondRedisHandler);

    }

    private static void populateDatabase(RedisHandler firstRedisHandler, RedisHandler secondRedisHandler){

        int firstDataSize = DataUtil.readIntFromEnv("FIRST_DATA_SIZE", 10000);
        int secondDataSize = DataUtil.readIntFromEnv("SECOND_DATA_SIZE", 12000);
        int desiredRange = DataUtil.readIntFromEnv("DESIRED_RANGE", 3);

        System.out.println("Parameters provided: \n");
        System.out.println("FIRST_DATA_SIZE: " + firstDataSize);
        System.out.println("SECOND_DATA_SIZE: " + secondDataSize);
        System.out.println("DESIRED_RANGE: " + desiredRange);

        // Generate 10000 random keys and values
        Map<String, String> entries = DataUtil.generatePairs(firstDataSize, null,desiredRange);
        //Generate another 12000 random keys and values
        Map<String, String> longEntries = DataUtil.generatePairs(secondDataSize, entries,desiredRange);

        // Populate the Redis instances with the generated data
        firstRedisHandler.consolidate(entries);
        secondRedisHandler.consolidate(longEntries);

        //Perform join operations
        var joinAlgorithms = new JoinAlgorithms(firstRedisHandler, secondRedisHandler);
        var pipelinedJoin = joinAlgorithms.pipelinedHashJoin(desiredRange);
        System.out.println("Pipelined join result: " + pipelinedJoin.size() );
        var semijoin = joinAlgorithms.semiJoin(3);
        System.out.println("Semi join result: " + semijoin.size());

        //Close the Redis connections
        firstRedisHandler.close();
        secondRedisHandler.close();
    }
}