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
- **Database**: PostgreSQL (production) / H2 (local development)
- **Cache**: Redis (production) / In-memory ConcurrentHashMap (local development)
- **Containerization**: Docker + Docker Compose

## Local Development

For local development, a `local` profile is available that does not require external services:

- **Database**: H2 in-memory database (auto-created on startup)
- **Cache**: In-memory `LocalCacheService` using `ConcurrentHashMap`
- **Activation**: Run with `-Dspring-boot.run.profiles=local`

### Seed Data

The `DataLoader` component loads sample data on startup for the local profile:
- 3 stores: Steam, PlayStation Store, Xbox Store
- 5 games: Zelda, Mario, God of War, Cyberpunk, Minecraft
- 10 prices: Various price points for testing

## Layer Responsibilities

### Controller Layer
- `GameController` - Game search endpoints
- `PriceController` - Price comparison endpoints
- `StoreController` - Store information endpoints

### Service Layer
- `GameService` - Game search and persistence (DB operations)
- `PriceAggregationService` - Price comparison logic, collector orchestration, result aggregation
- `CacheService` / `LocalCacheService` - Caching operations with 6-hour TTL (interface: `ICacheService`)

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
5. Collectors fetch prices from external stores (real API for Steam, mock for others)
6. Results are aggregated and normalized
7. Results are cached in Redis with 6-hour TTL
8. Response returned to user

## Caching Strategy
- **Cache Key Patterns**:
  - Search: `search:{query}`
  - Prices: `price:{gameId}`
- **TTL**: 6 hours for all cached data
- **Cache Invalidation**: Manual via service methods

## Steam Integration

The `SteamCollector` implementation uses Steam's public APIs to fetch game data and prices.

### API Endpoints

| Endpoint | Description |
|----------|-------------|
| `https://store.steampowered.com/api/storesearch/?term={query}` | Search games by name |
| `https://store.steampowered.com/api/appdetails?appids={appId}` | Get game details and price |

### Implementation Details

- **HTTP Client**: Spring WebClient with reactive programming
- **Timeout**: 5 seconds for all API calls
- **Error Handling**: Graceful degradation - returns empty results on failure
- **Caching**: Results cached with 6-hour TTL
  - Search cache key: `steam:search:{query}`
  - Price cache key: `steam:price:{appId}`
- **Regional Settings**: Argentina (cc=AR), Spanish language (l=spanish)

### Data Mapping

**Search Response:**
- Extracts: name, appId, price (in cents), tiny_image URL
- Maps to: StorePriceResult with PC platform, USD currency

**App Details Response:**
- Extracts: name, price_overview (final/original price, discount percent, currency)
- Maps to: StorePriceResult with full price information

### DTOs

| DTO | Description |
|-----|-------------|
| `SteamSearchResponse` | Response wrapper with total count and items list |
| `SteamItem` | Individual game item: id, name, price, tiny_image |
| `SteamPrice` | Price details: currency, initial, final (in cents) |

### Data Mapping

**Search Response:**
- Extracts: name, appId, thumbnail URL
- Maps to: StorePriceResult with PC platform

**App Details Response:**
- Extracts: name, price_overview (final/original price, discount percent, currency)
- Maps to: StorePriceResult with full price information

### Configuration

WebClient configured in `WebClientConfig` with:
- Base URL: `https://store.steampowered.com`
- Headers: Accept: application/json, User-Agent: GamePriceComparator/1.0
- Timeout: 5 seconds

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
- Application config via `application.yml` (and `application-local.yml` for local development)
- Store-specific config in database
- Environment variables for sensitive data
- Redis configuration with Jackson serialization (production)
- In-memory cache for local development

## Docker Architecture
- `app` - Spring Boot application
- `postgres` - PostgreSQL database
- `redis` - Redis cache

**Note**: For local development, use the `local` profile which runs without Docker dependencies.