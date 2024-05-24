# Capture the Flag (Praktikum Software Engineering FSS24)

## Requirements Specification

The (up-to-date) **requirements specification** can always be found in [REQUIREMENTS_SPECIFICATION.md](REQUIREMENTS_SPECIFICATION.md).

## Project

A Spring Boot project with integrated webserver.

Requirements

* Java 17
* Maven (Wrapper, see https://maven.apache.org/wrapper/)

### Build

Linux/MacOS

```bash
./mvnw clean install
```

Windows

```bash
mvnw.cmd clean install
```

### Run

In root directory of the project

```bash
java -jar target/ctf-0.0.1-SNAPSHOT.jar
```

### Integrated Webservice

Configuration (e.g., port) is located in [application.properties](src%2Fmain%2Fresources%2Fapplication.properties).

## Documentation

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.2/maven-plugin/reference/html/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.2/reference/htmlsingle/index.html#web)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)