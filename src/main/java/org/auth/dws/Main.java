package org.auth.dws;

import java.util.Map;

public class Main {
    public static void main(String[] args) {

        //Initialize two RedisHandler instances
        //TODO host and post should be read from config file or from environment variables, not really a problem since. Same applies for desiredRange
        var firstRedisHandler = new RedisHandler("redis1", 6379);
        var secondRedisHandler = new RedisHandler("redis2", 6378);

        // Populate the Redis instances with data'
        populateDatabase(firstRedisHandler, secondRedisHandler);

    }

    private static void populateDatabase(RedisHandler firstRedisHandler, RedisHandler secondRedisHandler){

        // Generate 10000 random keys and values
        Map<String, String> entries = DataUtil.generatePairs(10000, null,3);
        //Generate another 12000 random keys and values
        Map<String, String> longEntries = DataUtil.generatePairs(12000, entries,3);

        // Populate the Redis instances with the generated data
        firstRedisHandler.consolidate(entries);
        secondRedisHandler.consolidate(longEntries);

        //Perform join operations
        var joinAlgorithms = new JoinAlgorithms(firstRedisHandler, secondRedisHandler);
        var pipelinedJoin = joinAlgorithms.pipelinedHashJoin(3);
        System.out.println("Pipelined join result: " + pipelinedJoin.size() );
        var semijoin = joinAlgorithms.semiJoin(3);
        System.out.println("Semi join result: " + semijoin.size());

        //Close the Redis connections
        firstRedisHandler.close();
        secondRedisHandler.close();
    }
}