# Music Catalog Insights Platform — Endpoint Summary

## Setup

```bash
# 1. Start Postgres
docker-compose up -d

# 2. Build & run
./mvnw spring-boot:run

# Or without Maven installed, use:
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

---

## Auth — `/api/auth`

### POST /api/auth/register

Create a new user account.

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"secret123"}'
```

**Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "alice@test.com"
}
```

**Response (400) — duplicate email:**
```json
{
  "status": 400,
  "message": "Email already registered",
  "timestamp": "2026-07-28T12:00:00Z",
  "path": "uri=/api/auth/register"
}
```

### POST /api/auth/login

Authenticate and receive a JWT.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"secret123"}'
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "alice@test.com"
}
```

**Response (400) — bad credentials:**
```json
{
  "status": 400,
  "message": "Invalid email or password",
  "timestamp": "2026-07-28T12:00:00Z",
  "path": "uri=/api/auth/login"
}
```

---

## Search — `/api/search` (public)

### GET /api/search

Proxies to the iTunes Search API. Results are cached for 10 minutes.

```bash
curl "http://localhost:8080/api/search?query=beatles&type=song&limit=3"
```

**Response (200):**
```json
[
  {
    "appleCatalogId": 400835438,
    "title": "Here Comes the Sun",
    "artistName": "The Beatles",
    "genre": "Rock",
    "releaseDate": "1969-09-26",
    "durationSeconds": 185,
    "artworkUrl": "https://is1-ssl.mzstatic.com/image/thumb/..."
  }
]
```

---

## Library — `/api/library` (authenticated)

All library endpoints require the `Authorization: Bearer <token>` header. The user is
extracted from the JWT — never from the request body.

### GET /api/library

List saved songs for the authenticated user.

```bash
curl "http://localhost:8080/api/library?page=0&size=20&genre=Rock&year=2024" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "appleCatalogId": 400835438,
      "title": "Here Comes the Sun",
      "artistName": "The Beatles",
      "genre": "Rock",
      "releaseDate": "1969-09-26",
      "durationSeconds": 185,
      "artworkUrl": "https://...",
      "userRating": null,
      "userNotes": null,
      "createdAt": "2026-07-28T12:00:00Z",
      "updatedAt": "2026-07-28T12:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Optional query params: `page` (default 0), `size` (default 20, max 100), `genre`, `year`.

### POST /api/library

Save a song from a search result into the user's library.

```bash
curl -X POST http://localhost:8080/api/library \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "appleCatalogId": 400835438,
    "title": "Here Comes the Sun",
    "artistName": "The Beatles",
    "genre": "Rock",
    "releaseDate": "1969-09-26",
    "durationSeconds": 185,
    "artworkUrl": "https://..."
  }'
```

**Response (201):**
```json
{
  "id": 1,
  "appleCatalogId": 400835438,
  "title": "Here Comes the Sun",
  "artistName": "The Beatles",
  "genre": "Rock",
  "releaseDate": "1969-09-26",
  "durationSeconds": 185,
  "artworkUrl": "https://...",
  "userRating": null,
  "userNotes": null,
  "createdAt": "2026-07-28T12:00:00Z",
  "updatedAt": "2026-07-28T12:00:00Z"
}
```

**Required fields:** `appleCatalogId`, `title`, `artistName`.

### PUT /api/library/{id}

Update user rating (1–5) and/or notes. Only the owner can update.

```bash
curl -X PUT http://localhost:8080/api/library/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"userRating": 5, "userNotes": "Absolute classic!"}'
```

**Response (200):**
```json
{
  "id": 1,
  "userRating": 5,
  "userNotes": "Absolute classic!",
  ...
}
```

**Response (404) — not found or wrong user:**
```json
{
  "status": 404,
  "message": "Library item not found or does not belong to user",
  "timestamp": "2026-07-28T12:00:00Z",
  "path": "uri=/api/library/999"
}
```

### DELETE /api/library/{id}

Remove a saved song. Only the owner can delete.

```bash
curl -X DELETE http://localhost:8080/api/library/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Response (204)** — no body.

**Response (404)** — same error body as PUT above.

---

## Error response format (all errors)

```json
{
  "status": 4XX/5XX,
  "message": "Human-readable message",
  "timestamp": "2026-07-28T12:00:00Z",
  "path": "uri=/api/library/999"
}
```

---

## Assumptions

1. **iTunes API entity**: Searches always use `entity=song` as required (the `type` query param maps to `entity`).
2. **Media type**: Hardcoded to `media=music` since this is a music catalog.
3. **Artwork URL**: The iTunes API returns `artworkUrl100` (100×100); stored as-is. Frontend can swap `100x100bb` for larger sizes.
4. **Release date**: Truncated to `yyyy-MM-dd` from the full ISO-8601 timestamp.
5. **Duration**: `trackTimeMillis` is divided by 1000 and stored as integer seconds.
6. **JWT secret default**: A placeholder hex string is provided for local dev. **Must be changed in production** to a cryptographically random Base64 key ≥ 256 bits.
7. **User persistence**: No profile fields beyond email + password hash are stored.
8. **Search limit**: Capped at 50 to stay within iTunes API limits and prevent abuse.
9. **Pagination**: Default page 0, size 20, max 100. Sorted by `createdAt DESC`.
10. **Ownership**: All library operations scope data by extracting the user ID from the JWT; the `user_id` field is never accepted from request bodies.
