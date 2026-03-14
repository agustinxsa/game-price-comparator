# Game Price Comparator

A backend system that compares digital video game prices across multiple online stores (Steam, PlayStation Store, Xbox Store).

## Quick Start

```bash
# Build the project
mvn clean install

# Run with Docker
docker-compose up
```

## Features

- Price comparison across multiple game stores
- Game search with price aggregation
- Redis caching for improved performance
- Pluggable collector architecture for easy store additions

## Tech Stack

- Java 21
- Spring Boot 3.x
- PostgreSQL
- Redis
- Maven
- Docker

## Architecture and Specs

This project follows a **spec-driven development** approach. The specifications are located in the `/specs` directory and serve as the source of truth for the system.

### Specification Files

| File | Description |
|------|-------------|
| `specs/01-product-overview.md` | Project goals, non-functional requirements |
| `specs/02-architecture.md` | System architecture, components, data flow |
| `specs/03-domain-model.md` | Entities, relationships, indexes |
| `specs/04-api.md` | API endpoints, request/response formats |

### Workflow

1. **Update specs first** - Before making code changes, update the relevant spec file(s)
2. **Review specs** - Team members review specifications
3. **Implement changes** - Code must match specifications exactly
4. **Verify** - Ensure implementation aligns with specs

### Project Structure

```
game-price-comparator/
├── specs/                  # System specifications
│   ├── 01-product-overview.md
│   ├── 02-architecture.md
│   ├── 03-domain-model.md
│   ├── 04-api.md
│   └── README.md
├── src/
│   └── main/
│       └── java/
│           └── com/gameprice/comparator/
│               ├── collector/    # Store integrations
│               ├── config/       # Configuration
│               ├── controller/  # REST controllers
│               ├── dto/          # Data transfer objects
│               ├── entity/      # JPA entities
│               ├── enums/        # Enumerations
│               ├── repository/   # Data access
│               └── service/      # Business logic
└── pom.xml
```

## API Endpoints

- `GET /api/v1/games/search?query={query}` - Search games
- `GET /api/v1/prices/compare/{gameId}` - Get price comparison
- `GET /api/v1/prices/search/compare?query={query}` - Search with prices
- `GET /api/v1/stores` - List all stores

## Extending the System

To add a new store:

1. Implement `StoreCollector` interface
2. Add `@Component` annotation for auto-discovery
3. Add store entry to database with matching code

See `specs/README.md` for detailed workflow.