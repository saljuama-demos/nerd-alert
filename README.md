# Nerd alert
The definitive geek social network!

## Description
This is a demo project to demonstrate the usage of `jOOQ` as an alternative to JPA/Hibernate, so you can compare both
frameworks and tools, and make an informed decision on which one is right for your project. 

## Requirements
* Java 11 (8 by changing the JDK target at `build.gradle.kts`)
* Docker

## Quick Start

### Tests
To be able to run the tests, the database needs to be up and running
```bash
docker-compose -f docker/db-test.yml up -d
```
Tests can be run via IDE (either as JUNIT or Gradle task) or with the following command:
```bash
./gradlew test
```

### Running the application
The database for running the application can be spin up with
```bash
docker-compose -f docker/db-local.yml up -d
```
To run the app, can be done either via the IDE or with the following command: 
```bash
./gradlew bootRun
```
