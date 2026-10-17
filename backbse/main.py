from __future__ import annotations

import hashlib
import hmac
import os
import secrets
import sqlite3
from datetime import datetime, timedelta, timezone
from pathlib import Path

import jwt
from fastapi import Depends, FastAPI, Header, HTTPException, status
from pydantic import BaseModel, Field

BASE_DIR = Path(__file__).resolve().parent
DB_PATH = Path(os.getenv("FIRSTAPP_DB", BASE_DIR / "firstapp.db"))
JWT_SECRET = os.getenv("FIRSTAPP_JWT_SECRET", "change-this-development-secret")
JWT_ALGORITHM = "HS256"
ACCESS_MINUTES = 30
REFRESH_DAYS = 30

app = FastAPI(title="Firstapp API", version="1.0.0")


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


def db() -> sqlite3.Connection:
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    return connection


def init_db() -> None:
    with db() as connection:
        connection.executescript(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                phone TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                is_vip INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS refresh_tokens (
                token_hash TEXT PRIMARY KEY,
                user_id INTEGER NOT NULL,
                expires_at TEXT NOT NULL,
                revoked INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(user_id) REFERENCES users(id)
            );
            """
        )


@app.on_event("startup")
def startup() -> None:
    init_db()


class Credentials(BaseModel):
    phone: str = Field(min_length=5, max_length=32)
    password: str = Field(min_length=6, max_length=128)


class RefreshRequest(BaseModel):
    refresh_token: str = Field(min_length=20)


class UserResponse(BaseModel):
    id: int
    phone: str
    is_vip: bool


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user: UserResponse


class MessageResponse(BaseModel):
    message: str


class VipSiteResponse(BaseModel):
    url: str


def hash_password(password: str, salt: bytes | None = None) -> str:
    salt = salt or secrets.token_bytes(16)
    digest = hashlib.pbkdf2_hmac("sha256", password.encode(), salt, 120_000)
    return f"{salt.hex()}${digest.hex()}"


def verify_password(password: str, encoded: str) -> bool:
    salt_hex, digest_hex = encoded.split("$", 1)
    candidate = hashlib.pbkdf2_hmac(
        "sha256", password.encode(), bytes.fromhex(salt_hex), 120_000
    ).hex()
    return hmac.compare_digest(candidate, digest_hex)


def user_response(row: sqlite3.Row) -> UserResponse:
    return UserResponse(id=row["id"], phone=row["phone"], is_vip=bool(row["is_vip"]))


def create_access_token(user_id: int) -> str:
    now = utc_now()
    payload = {"sub": str(user_id), "type": "access", "iat": now, "exp": now + timedelta(minutes=ACCESS_MINUTES)}
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def create_refresh_token(user_id: int) -> str:
    raw = secrets.token_urlsafe(48)
    expires = utc_now() + timedelta(days=REFRESH_DAYS)
    with db() as connection:
        connection.execute(
            "INSERT INTO refresh_tokens(token_hash, user_id, expires_at) VALUES (?, ?, ?)",
            (hashlib.sha256(raw.encode()).hexdigest(), user_id, expires.isoformat()),
        )
    return raw


def issue_tokens(row: sqlite3.Row) -> TokenResponse:
    return TokenResponse(
        access_token=create_access_token(row["id"]),
        refresh_token=create_refresh_token(row["id"]),
        user=user_response(row),
    )


def get_current_user(authorization: str | None = Header(default=None)) -> sqlite3.Row:
    if not authorization or not authorization.lower().startswith("bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="缺少登录令牌")
    token = authorization.split(" ", 1)[1].strip()
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        if payload.get("type") != "access":
            raise ValueError("wrong token type")
        user_id = int(payload["sub"])
    except (jwt.PyJWTError, KeyError, TypeError, ValueError):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="登录令牌无效或已过期")
    with db() as connection:
        row = connection.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()
    if row is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="用户不存在")
    return row


@app.get("/api/health", response_model=MessageResponse)
def health() -> MessageResponse:
    return MessageResponse(message="ok")


@app.post("/api/auth/register", response_model=TokenResponse, status_code=201)
def register(credentials: Credentials) -> TokenResponse:
    with db() as connection:
        existing = connection.execute("SELECT id FROM users WHERE phone = ?", (credentials.phone,)).fetchone()
        if existing:
            raise HTTPException(status_code=409, detail="手机号已注册")
        cursor = connection.execute(
            "INSERT INTO users(phone, password_hash, is_vip, created_at) VALUES (?, ?, 0, ?)",
            (credentials.phone, hash_password(credentials.password), utc_now().isoformat()),
        )
        row = connection.execute("SELECT * FROM users WHERE id = ?", (cursor.lastrowid,)).fetchone()
    return issue_tokens(row)


@app.post("/api/auth/login", response_model=TokenResponse)
def login(credentials: Credentials) -> TokenResponse:
    with db() as connection:
        row = connection.execute("SELECT * FROM users WHERE phone = ?", (credentials.phone,)).fetchone()
    if row is None or not verify_password(credentials.password, row["password_hash"]):
        raise HTTPException(status_code=401, detail="手机号或密码错误")
    return issue_tokens(row)


@app.post("/api/auth/refresh", response_model=TokenResponse)
def refresh(request: RefreshRequest) -> TokenResponse:
    token_hash = hashlib.sha256(request.refresh_token.encode()).hexdigest()
    with db() as connection:
        token_row = connection.execute(
            "SELECT * FROM refresh_tokens WHERE token_hash = ? AND revoked = 0", (token_hash,)
        ).fetchone()
        if token_row is None or datetime.fromisoformat(token_row["expires_at"]) <= utc_now():
            raise HTTPException(status_code=401, detail="刷新令牌无效或已过期")
        connection.execute("UPDATE refresh_tokens SET revoked = 1 WHERE token_hash = ?", (token_hash,))
        user = connection.execute("SELECT * FROM users WHERE id = ?", (token_row["user_id"],)).fetchone()
    return issue_tokens(user)


@app.get("/api/me", response_model=UserResponse)
def me(user: sqlite3.Row = Depends(get_current_user)) -> UserResponse:
    return user_response(user)


@app.get("/api/permissions")
def permissions(user: sqlite3.Row = Depends(get_current_user)) -> dict:
    return {"calculator": True, "audio": True, "video": True, "vip": bool(user["is_vip"])}


@app.get("/api/vip/site", response_model=VipSiteResponse)
def vip_site(user: sqlite3.Row = Depends(get_current_user)) -> VipSiteResponse:
    if not bool(user["is_vip"]):
        raise HTTPException(status_code=403, detail="当前账号不是 VIP")
    return VipSiteResponse(url="https://v.cctv.com/")


@app.post("/api/auth/logout", response_model=MessageResponse)
def logout(request: RefreshRequest) -> MessageResponse:
    token_hash = hashlib.sha256(request.refresh_token.encode()).hexdigest()
    with db() as connection:
        connection.execute("UPDATE refresh_tokens SET revoked = 1 WHERE token_hash = ?", (token_hash,))
    return MessageResponse(message="已退出登录")
