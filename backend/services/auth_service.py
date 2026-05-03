import os
from fastapi import HTTPException, Request
from itsdangerous import URLSafeSerializer, BadSignature
from dotenv import load_dotenv
from models.user import User
from models.db import SessionLocal

load_dotenv()

_signer = URLSafeSerializer(os.getenv("SECRET_KEY", "dev-secret"), salt="session")
COOKIE_NAME = "session"


def create_session_cookie(user_id: int) -> str:
    return _signer.dumps({"user_id": user_id})


def get_current_user(request: Request) -> User | None:
    token = request.cookies.get(COOKIE_NAME)
    if not token:
        return None
    try:
        data = _signer.loads(token)
    except BadSignature:
        return None
    db = SessionLocal()
    try:
        return db.get(User, data["user_id"])
    finally:
        db.close()


def require_admin(user: User | None) -> User:
    if user is None or user.role != "admin":
        raise HTTPException(status_code=403, detail="Admin required")
    return user
