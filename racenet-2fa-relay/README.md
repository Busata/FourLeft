# racenet-2fa-relay

Captures EA's "Security Code" 2FA e-mails and relays the latest code to the
`racenet-authenticator` login flow.

When EA challenges the login with 2FA, the code arrives by e-mail. A forward rule
sends that mail to a [Postmark inbound webhook](https://postmarkapp.com/developer/webhooks/inbound-webhook),
which Postmark POSTs to this service. The service parses the 6-digit code, keeps
the most recent one in memory (with a 10-minute TTL), and serves it on a
token-protected endpoint.

## Endpoints

| Method | Path                | Auth                         | Purpose                                              |
|--------|---------------------|------------------------------|------------------------------------------------------|
| POST   | `/webhook/inbound`  | optional HTTP Basic          | Postmark inbound webhook target. Parses & stores the code. |
| GET    | `/code`             | `Authorization: Bearer …` or `X-Api-Token` | Returns `{"code": "199074"}`, or 404 if none/expired. |
| POST   | `/code/clear`       | `Authorization: Bearer …` or `X-Api-Token` | Clears the stored code. Call before starting a login. |
| GET    | `/health`           | none                         | Liveness check.                                      |

A new inbound mail **overwrites** the stored code. Codes expire after
`CODE_TTL_SECONDS` (default 600). The store is in-memory, so a restart drops the
current code — fine, since codes are short-lived anyway.

## Configuration

Copy `.env.example` to `.env` and fill it in:

- `API_TOKEN` *(required)* — protects `/code` and `/code/clear`. Generate with `openssl rand -hex 32`.
- `CODE_TTL_SECONDS` — code lifetime in seconds (default `600`).
- `WEBHOOK_USERNAME` / `WEBHOOK_PASSWORD` — optional HTTP Basic Auth for the webhook.

## Run

```bash
cp .env.example .env   # then edit
docker compose up --build -d
```

The container listens on `8085` and is reached through the `nginx-proxy` edge
(the same `VIRTUAL_HOST` / `LETSENCRYPT_HOST` mechanism the rest of the stack
uses) at `https://2fa-relay.fourleft.io`. It is not published on the host
directly. Point the `2fa-relay.fourleft.io` DNS record at this server.

## Postmark setup

Point the server's inbound webhook URL at the public address:

```
https://2fa-relay.fourleft.io/webhook/inbound
```

If you set `WEBHOOK_USERNAME` / `WEBHOOK_PASSWORD`, embed them in the URL so
Postmark sends them as Basic Auth:

```
https://USER:PASS@2fa-relay.fourleft.io/webhook/inbound
```

## Typical login flow (consumer side)

```bash
# 1. Clear any stale code before triggering the login
curl -X POST -H "Authorization: Bearer $API_TOKEN" https://2fa-relay.fourleft.io/code/clear

# 2. Trigger the EA login (which sends the 2FA mail) ...

# 3. Poll for the code (404 until it arrives)
curl -H "Authorization: Bearer $API_TOKEN" https://2fa-relay.fourleft.io/code
# -> {"code":"199074"}
```
