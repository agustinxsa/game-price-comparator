# Domain Model

## Entities

### Game
Represents a video game in the system.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| externalId | String | External store-specific ID |
| name | String | Game title |
| description | String | Game description |
| imageUrl | String | Cover image URL |
| releaseDate | LocalDate | Release date |
| platforms | List<Platform> | Supported platforms |
| createdAt | Timestamp | Creation timestamp |
| updatedAt | Timestamp | Last update timestamp |

### Store
Represents a digital game store.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| name | String | Store name (Steam, PlayStation, Xbox) |
| code | String | Unique store code |
| baseUrl | String | Store base URL |
| isActive | Boolean | Whether store is active |
| priority | Integer | Priority for price comparison |

### Price
Represents a price entry for a game at a specific store.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| gameId | Long | Foreign key to Game |
| storeId | Long | Foreign key to Store |
| amount | BigDecimal | Price amount |
| currency | String | Currency code (USD, EUR, etc.) |
| originalAmount | BigDecimal | Original price before discount |
| discountPercent | Integer | Discount percentage |
| url | String | Direct link to product page |
| collectedAt | Timestamp | When price was collected |
| isAvailable | Boolean | Whether game is available |

### PriceHistory
Historical price data for analytics.

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| priceId | Long | Foreign key to Price |
| amount | BigDecimal | Price at this point in time |
| recordedAt | Timestamp | When this price was recorded |

## Enumerations

### Platform
- PC
- PLAYSTATION
- XBOX
- NINTENDO
- MOBILE

### Currency
- USD
- EUR
- GBP
- JPY
- AUD
- CAD

## Relationships
- Game has many Prices (one per store)
- Store has many Prices
- Price has many PriceHistory entries

## Indexes
- Game.name (for search)
- Price.gameId + Price.storeId (unique constraint)
- PriceHistory.priceId + PriceHistory.recordedAt