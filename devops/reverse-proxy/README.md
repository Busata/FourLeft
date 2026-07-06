# fourleft.io reverse proxy

nginx that fronts `fourleft.io` and routes to the services:

| Path | Target |
|---|---|
| `/` | frontend |
| `/api_v2/` | backend-ea-sports-wrc |
| `/acrally-api/` | backend-acrally |
| `/acrally-agent/` | **static** — the acrally-agent release channel |

## acrally-agent release channel

`/acrally-agent/` serves static files straight from nginx, off the host directory
`/mnt/docker-data/acrally-agent/` (mounted read-only into this container in
`docker-compose.yml`). Publishing a new agent release is just uploading files —
no image rebuild or redeploy:

```
/mnt/docker-data/acrally-agent/
  latest.json                        # version pointer, cached ~60s
  acrally-agent-<version>.exe         # signed build, cached immutable
  acrally-agent-<version>.exe.minisig # detached minisign signature
```

The agent polls `https://fourleft.io/acrally-agent/latest.json`, then downloads
the versioned exe + `.minisig` and verifies the signature before self-replacing.
Upload the exe and signature first, then `latest.json` last, so the pointer never
references a file that isn't up yet. This is automated — from WSL, the `fourleft`
CLI → "Release AC Rally agent" builds, signs, and publishes in one step. Full
build/sign details: `acrally-agent/DISTRIBUTION.md`.

Create the dir once on the host before the first release:
`mkdir -p /mnt/docker-data/acrally-agent`.
