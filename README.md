# LavaSrc Failover Plugin

A [Lavalink](https://github.com/lavalink-devs/Lavalink) plugin that monitors the health of all audio sources (Spotify, Deezer, Apple Music, YouTube, SoundCloud, etc.) and exposes real-time health data via logs and a REST API.

## Features

- ✅ Monitors all LavaSrc sources automatically
- 📊 Tracks success/failure counts, success rate, and consecutive failures
- 🔴 Marks sources as `HEALTHY`, `DEGRADED`, or `DOWN` automatically
- 📡 REST API with detailed stats per source
- 📝 Logs warnings when a source starts failing
- 🔄 Reset stats per source via API

## Compatibility

| Lavalink | LavaSrc | Plugin |
|----------|---------|--------|
| 4.2.x    | 4.8.x   | 1.0.0  |

## Installation

### Option 1 — Compile manually (Termux / Linux)

Requirements: JDK 17+, Gradle 8+

```bash
git clone https://github.com/doyimmiuink/lavasrc-failover-plugin
cd lavasrc-failover-plugin
gradle jar
```

The `.jar` will be generated at `build/libs/lavasrc-failover-plugin-1.0.0.jar`.

Copy it to your Lavalink `plugins/` folder.

### Option 2 — Add to application.yml (recommended)

```yaml
lavalink:
  plugins:
    - dependency: "dev.lavalink.failover:lavasrc-failover-plugin:1.0.0"
      repository: "https://maven.pkg.github.com/doyimmiuink/lavasrc-failover-plugin"
```

## Configuration

Add to your `application.yml`:

```yaml
plugins:
  failover:
    enabled: true
    logLevel: INFO       # INFO or DEBUG
    failureThreshold: 3  # consecutive failures before marking source as DOWN
    endpointEnabled: true
    endpointPath: /v4/failover/health
```

## REST Endpoints

All endpoints require the Lavalink `Authorization` header.

### `GET /v4/failover/health`

Full health report for all sources.

```json
{
  "overall": "DEGRADED",
  "activeSources": 5,
  "downSources": 1,
  "sources": [
    {
      "name": "spotify",
      "status": "DOWN",
      "totalRequests": 42,
      "successCount": 30,
      "failureCount": 12,
      "consecutiveFailures": 6,
      "successRate": "71.4%",
      "lastSuccess": "2026-04-20T15:00:00Z",
      "lastFailure": "2026-04-20T15:30:00Z",
      "recentFailures": [
        { "timestamp": "...", "errorCode": 429, "message": "Too many requests" }
      ]
    }
  ]
}
```

### `GET /v4/failover/health/{source}`

Health for a single source. Valid sources: `spotify`, `deezer`, `applemusic`, `youtube`, `soundcloud`, `yandexmusic`, `ytdlp`.

### `GET /v4/failover/summary`

Quick summary — status + success rate per source.

```json
{
  "overall": "HEALTHY",
  "healthy": 6,
  "degraded": 0,
  "down": 0,
  "sources": [
    { "name": "deezer", "status": "HEALTHY", "successRate": "100.0%" }
  ]
}
```

### `POST /v4/failover/reset/{source}`

Reset stats for a source.

```json
{ "message": "Reset spotify stats successfully" }
```

## Source Status Logic

| Status | Condition |
|--------|-----------|
| `HEALTHY` | 0-1 consecutive failures |
| `DEGRADED` | 2-4 consecutive failures |
| `DOWN` | 5+ consecutive failures |

Any successful load resets the consecutive failure counter back to `HEALTHY`.

## License

MIT — made with ❤️ by [doyimmiuink](https://github.com/doyimmiuink)
