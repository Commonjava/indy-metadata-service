# indy-metadata-service


## Prerequisite
1. jdk11
2. mvn 3.6.2+

## Configure services

To make it run, Infinispan and Kafka need to be configured. Please see the example in `application.yaml`.

## Try it

### 1. Start the cache server and the message broker

It needs a remote cache server(Infinispan) as well as the message broker(Kafka) for event handler. To ease the setup, we have provided a `docker-compose.yml` file which start the containers and bind the network ports.

```
docker-compose up
```

### 2. Start gateway in debug mode
```
$ mvn quarkus:dev
```

### 3. Verify the installation 

```
http://localhost:8080/swagger-ui/
```

### 4. Check the entries on cache server

```
http://localhost:11222/console/
```



