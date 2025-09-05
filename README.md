# Indy Metadata Service

A Quarkus-based microservice for managing metadata operations in the Indy artifact repository system. This service handles metadata file management, special path operations, and provides REST APIs for metadata-related operations.

## Overview

The Indy Metadata Service is part of the CommonJava Indy ecosystem and provides:

- **Metadata Management**: Automatic cleanup and management of metadata files (e.g., `maven-metadata.xml`, `package.json`)
- **Group-Aware Operations**: Metadata updates propagate through repository group hierarchies
- **Event-Driven Architecture**: Reactive processing of file events via Kafka
- **RESTful APIs**: HTTP endpoints for metadata operations
- **Multi-Package Support**: Support for Maven, NPM, and other package types

## Architecture

### Core Components

- **MetadataController**: Main business logic controller
- **MetadataHandler**: Handles metadata deletion and cleanup operations
- **FileEventConsumer**: Processes file events from Kafka for automatic metadata cleanup
- **MetadataResource**: JAX-RS REST endpoints

## API Endpoints

### Metadata Operations

```http
GET    /api/metadata/{packageType}/{type}/{name}/{path}
DELETE /api/metadata/{packageType}/{type}/{name}/{path}
GET    /api/metadata/{packageType}/{type}/{name}
```

**Parameters:**
- `packageType`: Package type (maven, npm, etc.)
- `type`: Store type (hosted, group, remote)
- `name`: Store name
- `path`: File path within the store

## Configuration

### Application Properties

Key configuration options in `application.yaml`:

```yaml
quarkus:
  http:
    port: 8081
    read-timeout: 30m
    limits:
      max-body-size: 500M

kafka:
  bootstrap:
    servers: "localhost:9092"

storage-service-api/mp-rest/url: http://localhost
repo-service-api/mp-rest/url: http://localhost
```

### Environment Variables

- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses
- `STORAGE_SERVICE_API_MP_REST_URL`: Storage service URL
- `REPO_SERVICE_API_MP_REST_URL`: Repository service URL

## Development

### Prerequisites

- Java 11+
- Maven 3.6+
- Docker & Docker Compose (for local development)

### Building

```bash
# Compile and run tests
mvn clean compile test

# Package the application
mvn package

# Run in development mode
mvn quarkus:dev
```

### Docker Development

```bash
# Build the Docker image
docker build -f src/main/image/Dockerfile.jvm -t indy-metadata-service .

# Run with Docker Compose (includes Kafka)
docker-compose up
```

### Local Development Setup

1. Start Kafka and Zookeeper:
   ```bash
   docker-compose up zookeeper kafka
   ```

2. Configure external services (storage and repository services)

3. Run the application:
   ```bash
   mvn quarkus:dev
   ```

## Event Processing

The service processes two types of Kafka events and maintains consistency across repository group hierarchies:

> **Important**: When a metadata file is updated in any repository, the service automatically propagates these changes up through the entire group hierarchy. For example, if a POM file is uploaded to a hosted repository, the metadata will be regenerated not only for that repository but also for all groups that contain it, all the way up to the root group.

### File Events (`file-event` topic)
- Triggers metadata cleanup when POM files or package tarballs are uploaded/deleted
- Automatically clears corresponding metadata files in hosted repositories
- **Group Propagation**: Updates cascade through all repository groups containing the affected repository

### Promote Events (`promote-complete` topic)
- Handles promotion completion events
- Manages metadata updates across repository groups


## Monitoring & Observability

- **OpenTelemetry**: Distributed tracing support
- **Structured Logging**: JSON-formatted logs with configurable levels
- **Health Checks**: Built-in Quarkus health endpoints
- **Metrics**: Application metrics via Micrometer

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Run specific test class
mvn test -Dtest=MetadataResourceTest
```

## Dependencies

- **Quarkus**: Reactive framework and runtime
- **RESTEasy**: JAX-RS implementation
- **SmallRye Reactive Messaging**: Kafka integration
- **OpenTelemetry**: Observability
- **Maven Repository Metadata**: Maven-specific metadata handling

## License

Licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
