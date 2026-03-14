# Architecture

## System Architecture Overview
The system follows a layered architecture with clear separation of concerns:
- **Controller Layer** - Handles HTTP requests/responses
- **Service Layer** - Business logic and orchestration
- **Repository Layer** - Data access and persistence
- **Collector Layer** - Pluggable store integrations

## Technology Stack
- **Runtime**: Java 21
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven
- **Database**: PostgreSQL (primary data store)
- **Cache**: Redis (price caching, session management)
- **Containerization**: Docker + Docker Compose

## Layer Responsibilities

### Controller Layer
- `GameController` - Game search endpoints
- `PriceController` - Price comparison endpoints
- `StoreController` - Store information endpoints

### Service Layer
- `GameService` - Game search and persistence (DB operations)
- `PriceAggregationService` - Price comparison logic, collector orchestration, result aggregation
- `CacheService` - Redis caching operations with 6-hour TTL

### Repository Layer
- `GameRepository` - Game entity persistence
- `PriceRepository` - Price history persistence
- `StoreRepository` - Store configuration persistence

### Collector Layer (Pluggable)
- `StoreCollector` - Interface for store integrations (replaces PriceCollector)
  - `searchGames(String query)` - Search games by name
  - `fetchPrices(String externalId)` - Fetch prices for a specific game
- `SteamCollector` - Steam store implementation
- `PlayStationCollector` - PlayStation Store implementation
- `XboxCollector` - Xbox Store implementation

## Data Flow
1. User sends search request to API
2. Controller receives and validates request
3. Service checks Redis cache for cached results
4. If cache miss, PriceAggregationService orchestrates collector execution
5. Collectors fetch prices from external stores (mock implementations)
6. Results are aggregated and normalized
7. Results are cached in Redis with 6-hour TTL
8. Response returned to user

## Caching Strategy
- **Cache Key Patterns**:
  - Search: `search:{query}`
  - Prices: `price:{gameId}`
- **TTL**: 6 hours for all cached data
- **Cache Invalidation**: Manual via service methods

## Extensibility
New stores can be added by:
1. Implementing `StoreCollector` interface
2. Adding `@Component` annotation for auto-discovery
3. Adding store entry to database with matching code

## Component Diagram
```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Controllers │────▶│  Services   │────▶│ Repositories│
└─────────────┘     └─────────────┘     └─────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼─────┐ ┌────▼────┐ ┌────▼─────┐
        │  Game     │ │Price    │ │  Cache  │
        │ Service   │ │Aggregat.│ │ Service │
        └───────────┘ │Service  │ └─────────┘
                      └────┬────┘
                           │
                    ┌──────▼──────┐
                    │  Collectors │
                    ├─────────────┤
                    │   Steam     │
                    │ PlayStation │
                    │    Xbox     │
                    └─────────────┘
```

## Configuration
- Application config via `application.yml`
- Store-specific config in database
- Environment variables for sensitive data
- Redis configuration with Jackson serialization

## Docker Architecture
- `app` - Spring Boot application
- `postgres` - PostgreSQL database
- `redis` - Redis cache