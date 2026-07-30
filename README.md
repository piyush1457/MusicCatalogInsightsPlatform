# Music Catalog Insights Platform

A full-stack application for discovering, cataloging, and analyzing music from the iTunes Store. Users can search the iTunes catalog, build a personal music library, and get AI-powered insights about their collection.

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | React 19, TypeScript 6, Vite 8, TanStack React Query 5, Recharts 3, Framer Motion 12 |
| **Backend** | Spring Boot 3.2.4, Java 17, Maven |
| **Database** | PostgreSQL 16 (production), H2 in-memory (development) |
| **Auth** | JWT (jjwt 0.12.5), BCrypt |
| **Migrations** | Flyway |
| **Cache** | Caffeine |
| **APIs** | iTunes Search API, Groq AI (llama-3.3-70b) |
| **Infra** | Docker Compose, Render |

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Client (Browser)                         │
│  React SPA · React Router · TanStack Query · Recharts      │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP (localhost:5173 → localhost:8080)
                          ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (port 8080)                │
│                                                             │
│  ┌───────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ SecurityConfig│  │    Controllers   │  │GlobalException│ │
│  │ JwtAuthFilter │  │  Auth · Search   │  │   Handler    │ │
│  │   JwtUtil     │  │ Library · Analyst│  │              │ │
│  └───────────────┘  │ Insights         │  └──────────────┘ │
│                     └────────┬─────────┘                    │
│                              ▼                              │
│                     ┌────────────────┐                      │
│                     │   Services     │                      │
│                     │ Auth · Search  │──────────────────────┼──→ iTunes API
│                     │ Library · Ana. │──→ Groq AI API       │
│                     └────────┬───────┘                      │
│                              ▼                              │
│                     ┌────────────────┐                      │
│                     │  Repositories  │                      │
│                     │  (Spring Data) │                      │
│                     └────────┬───────┘                      │
└──────────────────────────────┼──────────────────────────────┘
                               │
                               ▼
              ┌────────────────────────────┐
              │  PostgreSQL / H2 Database  │
              │  users · library_items     │
              └────────────────────────────┘
```

### Backend Layers

- **Controllers** — REST endpoints for auth, search, library CRUD, analytics, and AI insights
- **Services** — Business logic including iTunes proxy with Caffeine caching, JWT auth, library management, analytics computation, and Groq AI integration
- **Repositories** — Spring Data JPA repositories for `User` and `LibraryItem` entities
- **Config** — JWT filter chain, CORS (allow localhost:5173 & 3000), BCrypt password encoder, Caffeine caches, RestTemplate with iTunes-compatible media type

### API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | — | Register a new user |
| POST | `/api/auth/login` | — | Login, returns JWT |
| GET | `/api/search?query=&type=&limit=` | — | Search iTunes catalog |
| GET | `/api/library?page=&size=&genre=&year=` | JWT | List user's library (paginated) |
| POST | `/api/library` | JWT | Add item to library |
| PUT | `/api/library/{id}` | JWT | Update rating/notes |
| DELETE | `/api/library/{id}` | JWT | Remove from library |
| GET | `/api/analytics/summary` | JWT | Library analytics (genre dist., top artists, etc.) |
| POST | `/api/insights/summary` | JWT | AI-generated insights via Groq |

### Frontend Structure

```
src/
├── main.tsx              # Entry point
├── App.tsx               # Router + providers
├── context/AuthContext   # JWT auth state (localStorage)
├── lib/apiClient         # Axios client with interceptor
├── hooks/useDebounce     # Search debounce hook
├── components/
│   ├── Navbar            # Sidebar navigation
│   ├── ProtectedRoute    # Auth guard wrapper
│   └── StarRating        # Reusable rating component
├── pages/
│   ├── Login / Register  # Auth pages
│   ├── Search            # iTunes search + save to library
│   ├── Library           # User's collection (CRUD)
│   └── Dashboard         # Analytics + AI insights
```

## Getting Started

### Prerequisites

- Java 17+
- Node.js 20+
- Docker (optional, for PostgreSQL)

### 1. Clone and Install

```bash
# Install frontend dependencies
cd frontend
npm install

# Build backend
cd ../backend
./mvnw clean package -DskipTests
```

### 2. Environment Setup

```bash
# Backend env (backend/.env) — already configured for dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=music_catalog
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=your-256-bit-base64-secret
GROQ_API_KEY=your-groq-api-key

# Frontend env (frontend/.env)
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Run with H2 (Development — No Docker Needed)

```bash
# Start backend (uses H2 in-memory db by default via dev profile)
cd backend
./mvnw spring-boot:run

# Start frontend (separate terminal)
cd frontend
npm run dev
```

Open http://localhost:5173

### 4. Run with PostgreSQL (via Docker)

```bash
# Start PostgreSQL
cd backend
docker-compose up -d

# Run backend with postgres profile
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Deployment

### Option A: Render (Recommended)

1. Push the repository to GitHub
2. Create a new **Blueprint** on Render from the `backend/render.yaml`
3. Set the following environment secrets in Render dashboard:
   - `JWT_SECRET` — secure random base64 string
   - `PGPASSWORD` — postgres password
   - `GROQ_API_KEY` — your Groq API key
4. For the frontend, create a **Static Site** on Render:
   - Build command: `cd frontend && npm install && npm run build`
   - Publish directory: `frontend/dist`
   - Set env var `VITE_API_BASE_URL` to your backend URL

### Option B: Docker

```bash
# Build backend image
cd backend
docker build -t music-catalog-backend .

# Run with docker-compose (includes PostgreSQL)
docker-compose up -d
```

### Option C: Manual (VPS)

**Backend:**
```bash
cd backend
./mvnw clean package -DskipTests
java -jar target/music-catalog-insights-*.jar --spring.profiles.active=postgres
```

**Frontend:**
```bash
cd frontend
npm run build
# Serve dist/ with nginx or any static server
```

## Database Migrations

Flyway manages schema changes. Migrations are in `backend/src/main/resources/db/migration/`.

- `V1__create_users_table.sql`
- `V2__create_library_items_table.sql`

Run automatically when `spring.flyway.enabled=true` (postgres profile).
