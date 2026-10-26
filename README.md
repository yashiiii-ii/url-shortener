# URL Shortener with Link Safety Scanner

A backend URL shortener built with **Spring Boot**, **PostgreSQL** and **JPA**. Unlike basic shorteners, it checks links for scam patterns before shortening and can redirect users based on their device.

## Features

- **Shorten URLs** using Base62 encoding of database IDs (unique, collision-free codes)
- **Custom aliases** (e.g. `/diwali-sale`) with duplicate checking
- **Link expiry**: expired links return `410 Gone`
- **Link safety scanner (rule-based)**: scores URLs using heuristics such as IP-address hosts, too many subdomains, punycode lookalike domains, chained shorteners and scam keywords
  - `BLOCKED`: link is rejected
  - `SUSPICIOUS`: user sees a warning page before continuing
  - `SAFE`: normal redirect
- **Smart redirects**: optional separate URL for mobile users, chosen via User-Agent
- **Async click analytics** (`@Async`): device and browser stats without slowing down redirects, with basic bot filtering
- Global exception handling with clean JSON errors

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4, Spring Web, Spring Data JPA |
| Database | PostgreSQL (Neon) |
| Build | Maven |
| Others | Lombok, Bean Validation |

## API

### Create a short link
`POST /api/urls`
```json
{
  "url": "https://www.example.com/some/very/long/path",
  "mobileUrl": "https://m.example.com",
  "customAlias": "demo",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```
Only `url` is required. Response (`201`):
```json
{
  "shortUrl": "http://localhost:8080/demo",
  "shortCode": "demo",
  "safetyStatus": "SAFE"
}
```

### Redirect
`GET /{code}` redirects (`302`) to the original URL (or the mobile URL on mobile devices).

### Analytics
`GET /api/urls/{code}/analytics` returns total clicks plus device and browser breakdowns.

## Run Locally

**Prerequisites:** Java 17 and a PostgreSQL database (local or [Neon](https://neon.tech))

1. Clone the repo
   ```
   git clone https://github.com/yashiiii-ii/url-shortener.git
   cd url-shortener
   ```
2. Create `src/main/resources/application.yml` with your database URL and username, and `password: ${DB_PASSWORD}`.
3. Set the password as an environment variable (PowerShell):
   ```
   $env:DB_PASSWORD="your_password"
   ```
4. Run
   ```
   ./mvnw spring-boot:run
   ```
5. App starts on `http://localhost:8080`

## Roadmap

- [ ] Redis caching for faster redirects (with benchmarks)
- [ ] JWT authentication and per-user links
- [ ] Rate limiting (Bucket4j)
- [ ] Google Safe Browsing API integration
- [ ] Unit and integration tests, GitHub Actions CI
- [ ] React analytics dashboard
- [ ] Docker and cloud deployment

## What I Learned

- Designing a read-heavy service and why redirects must stay fast (async analytics)
- Base62 encoding for short unique codes
- Layered architecture (controller, service, repository, DTO)
- Handling secrets safely with environment variables