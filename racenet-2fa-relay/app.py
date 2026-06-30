"""
racenet-2fa-relay

A tiny relay that captures EA's "Security Code" 2FA e-mails (forwarded into a
Postmark inbound webhook) and makes the most recent code available on a
token-protected endpoint, so the racenet-authenticator OAuth flow can read it.

Flow:
  1. EA sends a 2FA mail -> e-mail forward -> Postmark inbound -> POST /webhook/inbound
  2. The 6-digit code is parsed and stored in memory (overwriting any previous one),
     with a short TTL (default 10 minutes).
  3. The authenticator reads it from GET /code (Bearer / X-Api-Token protected).
  4. Before starting a new login, the authenticator calls POST /code/clear so a
     stale code can never be consumed by accident.
"""
import base64
import hmac
import os
import re
import threading
import time
from typing import Optional

from fastapi import Depends, FastAPI, Header, HTTPException, Request, status
from fastapi.responses import JSONResponse

# --- Configuration (via environment) -----------------------------------------

API_TOKEN = os.environ.get("API_TOKEN", "").strip()
CODE_TTL_SECONDS = int(os.environ.get("CODE_TTL_SECONDS", "600"))

# Optional HTTP Basic Auth on the inbound webhook. Postmark lets you embed
# credentials in the webhook URL (https://user:pass@host/webhook/inbound); when
# both are set we enforce them, otherwise the webhook is left open.
WEBHOOK_USERNAME = os.environ.get("WEBHOOK_USERNAME", "")
WEBHOOK_PASSWORD = os.environ.get("WEBHOOK_PASSWORD", "")

if not API_TOKEN:
    raise RuntimeError("API_TOKEN must be set; refusing to start without read protection.")

# --- In-memory store ----------------------------------------------------------

_lock = threading.Lock()
_code: Optional[str] = None
_expires_at: float = 0.0


def _store_code(code: str) -> None:
    global _code, _expires_at
    with _lock:
        _code = code
        _expires_at = time.time() + CODE_TTL_SECONDS


def _get_code() -> Optional[str]:
    global _code, _expires_at
    with _lock:
        if _code is None:
            return None
        if time.time() >= _expires_at:
            _code = None
            _expires_at = 0.0
            return None
        return _code


def _clear_code() -> None:
    global _code, _expires_at
    with _lock:
        _code = None
        _expires_at = 0.0


# --- Code extraction ----------------------------------------------------------

# A standalone 6-digit number (EA security codes are 6 digits). We prefer one
# that appears just after the "Security Code" label to avoid matching digits
# inside tracking URLs or the copyright year.
_SECURITY_CODE_LABEL = re.compile(r"security\s*code", re.IGNORECASE)
_SIX_DIGITS = re.compile(r"\b(\d{6})\b")
_SIX_DIGIT_LINE = re.compile(r"^\s*(\d{6})\s*$", re.MULTILINE)


def extract_code(*texts: Optional[str]) -> Optional[str]:
    """Pull the 6-digit EA security code out of the e-mail body."""
    for text in texts:
        if not text:
            continue

        # 1. Most reliable: the code sits on its own line in the plain-text mail.
        line_match = _SIX_DIGIT_LINE.search(text)
        if line_match:
            return line_match.group(1)

        # 2. Otherwise anchor on the "Security Code" label and take the next 6 digits.
        label = _SECURITY_CODE_LABEL.search(text)
        if label:
            after = _SIX_DIGITS.search(text, label.end())
            if after:
                return after.group(1)
    return None


# --- Auth helpers -------------------------------------------------------------

def _constant_time_eq(a: str, b: str) -> bool:
    return hmac.compare_digest(a.encode(), b.encode())


def require_api_token(
    authorization: Optional[str] = Header(default=None),
    x_api_token: Optional[str] = Header(default=None),
) -> None:
    """Protect the read/clear endpoints with a bearer token or X-Api-Token header."""
    provided = ""
    if authorization and authorization.lower().startswith("bearer "):
        provided = authorization[7:].strip()
    elif x_api_token:
        provided = x_api_token.strip()

    if not provided or not _constant_time_eq(provided, API_TOKEN):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or missing API token.",
        )


def verify_webhook_auth(request: Request) -> None:
    """Optional HTTP Basic Auth for the Postmark inbound webhook."""
    if not (WEBHOOK_USERNAME and WEBHOOK_PASSWORD):
        return  # auth disabled

    header = request.headers.get("authorization", "")
    if not header.lower().startswith("basic "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Webhook auth required.")
    try:
        decoded = base64.b64decode(header[6:].strip()).decode()
        user, _, password = decoded.partition(":")
    except Exception:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Malformed credentials.")

    if not (_constant_time_eq(user, WEBHOOK_USERNAME) and _constant_time_eq(password, WEBHOOK_PASSWORD)):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid webhook credentials.")


# --- App ----------------------------------------------------------------------

app = FastAPI(title="racenet-2fa-relay", docs_url=None, redoc_url=None, openapi_url=None)


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/webhook/inbound")
async def inbound(request: Request) -> JSONResponse:
    """Postmark inbound webhook. See https://postmarkapp.com/developer/webhooks/inbound-webhook"""
    verify_webhook_auth(request)

    try:
        payload = await request.json()
    except Exception:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Expected JSON body.")

    code = extract_code(
        payload.get("TextBody"),
        payload.get("StrippedTextReply"),
        payload.get("HtmlBody"),
        payload.get("Subject"),
    )

    if not code:
        # Always 200 so Postmark doesn't retry; just report we found nothing.
        return JSONResponse(status_code=status.HTTP_200_OK, content={"stored": False, "reason": "no code found"})

    _store_code(code)
    return JSONResponse(status_code=status.HTTP_200_OK, content={"stored": True})


@app.get("/code", dependencies=[Depends(require_api_token)])
def read_code() -> JSONResponse:
    code = _get_code()
    if code is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="No current code.")
    return JSONResponse(content={"code": code})


@app.post("/code/clear", dependencies=[Depends(require_api_token)])
def clear_code() -> JSONResponse:
    _clear_code()
    return JSONResponse(content={"cleared": True})
