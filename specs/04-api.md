# API Specification

## Base URL
`/api/v1`

## Endpoints

### 1. Search Games
**GET** `/games/search`

Query Parameters:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| query | String | Yes | Search term |
| page | Integer | No | Page number (default: 0) |
| size | Integer | No | Page size (default: 20) |

Response (200):
```json
{
  "content": [
    {
      "id": 1,
      "name": "Game Title",
      "description": "Game description",
      "imageUrl": "https://...",
      "releaseDate": "2024-01-15",
      "platforms": ["PC", "PLAYSTATION"]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### 2. Get Price Comparison
**GET** `/prices/compare/{gameId}`

Path Parameters:
| Parameter | Type | Description |
|-----------|------|-------------|
| gameId | Long | Game ID |

Response (200):
```json
{
  "game": {
    "id": 1,
    "name": "Game Title",
    "imageUrl": "https://..."
  },
  "prices": [
    {
      "store": {
        "id": 1,
        "name": "Steam",
        "code": "steam"
      },
      "amount": 29.99,
      "currency": "USD",
      "originalAmount": 59.99,
      "discountPercent": 50,
      "url": "https://store.steampowered.com/...",
      "isAvailable": true,
      "collectedAt": "2024-01-15T10:30:00Z"
    }
  ],
  "bestPrice": {
    "store": "Steam",
    "amount": 29.99,
    "currency": "USD"
  }
}
```

### 3. Get All Stores
**GET** `/stores`

Response (200):
```json
[
  {
    "id": 1,
    "name": "Steam",
    "code": "steam",
    "baseUrl": "https://store.steampowered.com",
    "isActive": true
  }
]
```

### 4. Search with Price Comparison
**GET** `/games/search/compare`

Query Parameters:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| query | String | Yes | Search term |

Response (200):
```json
{
  "results": [
    {
      "game": {
        "id": 1,
        "name": "Game Title",
        "imageUrl": "https://..."
      },
      "prices": [...],
      "bestPrice": {
        "store": "Steam",
        "amount": 29.99,
        "currency": "USD"
      }
    }
  ]
}
```

## Error Responses

### 400 - Bad Request
```json
{
  "error": "Bad Request",
  "message": "Query parameter is required",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 404 - Not Found
```json
{
  "error": "Not Found",
  "message": "Game not found",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 500 - Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Rate Limiting
- No rate limiting in v1 (subject to change)

## API Versioning
- URL-based versioning: `/api/v1/`
- Backward compatibility maintained within major version