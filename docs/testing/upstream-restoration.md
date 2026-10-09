# Position test restoration

The seven retained upstream suites now run as ordinary JUnit 5 tests: 67 serial/GPSd/parser/tracker/service cases and eight REST cases. Maven 3.10/JDK 21 reports no failures, errors or skips.

Serial fixtures use the fork's `CommConnectionFactory.createConnection(CommURI)` contract. Assertions inspect the URI object, retaining configured fields and timeout defaults instead of relying on the removed OSGi IO factory. GPSd endpoints and serial connections are mocked; no hardware or external daemon is contacted. Sample JSON comes from the classpath, so IDEA does not need a module-specific working directory. Every fixture closes its readers/providers and services, including failure paths. REST no-lock cases verify the current JAX-RS HTTP 500 response.

```sh
mvn test
```

The production code, DS descriptors, bundle resources and dependency versions are unchanged.
