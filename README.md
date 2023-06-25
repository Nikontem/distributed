# Distributed Data Join Algorithms

This Java project demonstrates the use of pipelined hash join and semi-join algorithms to merge two datasets stored in
distributed Redis databases. The join condition is based on key matching and date differences.
Installation

To run this project, you need a Java development environment (JDK 8 or higher) and Maven for dependency management.
Redis instances are also required for storing and retrieving data.
Usage

## Java Way

Before following this way, go to Main.java and change both redis hosts to localhost

Boot up two Redis instances in the following ports 6379 and 6378. This can be done either manually or using Docker.

```
docker run -d --name redis-stack2 -p 6378:6379 -p 8002:8002 redis/redis-stack:latest
docker run -d --name redis-stack1 -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

Then, run the following commands to build and run the project:

``` 
mvn clean install
java -jar target/distributed-1.0-SNAPSHOT-jar-with-dependencies.jar 
```

## Docker Way

A Dockerfile and docker-compose.yml file are provided for running the project in a Docker container.
Docker-Compose must be installed since the two Redis databases must be deployed before the app is deployed
To build the Docker image, navigate to project folder and run the following command:

```
docker-compose up
```

## Components

The main components of this project include:

* DataUtil: A utility class providing various helper methods related to data generation and processing.
* RedisHandler: Handles operations related to Redis, including data consolidation and retrieval.
* JoinAlgorithms: Contains the implementation of the pipelined hash join and semi-join algorithms.
* Main: The entry point of the program. Initializes Redis handlers, populates the Redis instances with data, and
  executes the join operations.

## Join Algorithms

Two join algorithms are implemented in this project:

1. Pipelined Hash Join: This algorithm creates a hash table from the smaller dataset and then scans the larger dataset
   for matching entries. The algorithm is efficient when a significant portion of keys match and both datasets are
   sizeable.

2. Semi Join: This algorithm is efficient when the smaller dataset is much smaller than the larger dataset and there is
   a low proportion of matching keys. The algorithm reduces data transfer by only sending keys from the smaller table to
   the larger table for matching.