import os
from fastapi import APIRouter, Request
from fastapi.responses import RedirectResponse, JSONResponse
import httpx
from dotenv import load_dotenv
from models.db import SessionLocal
from models.user import User
from services.auth_service import create_session_cookie, get_current_user, COOKIE_NAME

load_dotenv()

router = APIRouter()

GITHUB_CLIENT_ID = os.getenv("GITHUB_CLIENT_ID", "")
GITHUB_CLIENT_SECRET = os.getenv("GITHUB_CLIENT_SECRET", "")
GITHUB_AUTH_URL = "https://github.com/login/oauth/authorize"
GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
GITHUB_USER_URL = "https://api.github.com/user"
GITHUB_EMAIL_URL = "https://api.github.com/user/emails"


@router.get("/auth/login")
def login():
    url = f"{GITHUB_AUTH_URL}?client_id={GITHUB_CLIENT_ID}&scope=read:user,user:email"
    return RedirectResponse(url)


@router.get("/auth/callback")
def callback(code: str, request: Request):
    with httpx.Client() as client:
        token_resp = client.post(
            GITHUB_TOKEN_URL,
            data={"client_id": GITHUB_CLIENT_ID, "client_secret": GITHUB_CLIENT_SECRET, "code": code},
            headers={"Accept": "application/json"},
        )
        token_data = token_resp.json()
        access_token = token_data.get("access_token")
        if not access_token:
            return JSONResponse({"error": "OAuth failed"}, status_code=400)

        headers = {"Authorization": f"Bearer {access_token}", "Accept": "application/json"}
        user_resp = client.get(GITHUB_USER_URL, headers=headers)
        gh_user = user_resp.json()

        email = gh_user.get("email")
        if not email:
            emails_resp = client.get(GITHUB_EMAIL_URL, headers=headers)
            primary = next((e for e in emails_resp.json() if e.get("primary")), None)
            email = primary["email"] if primary else f"{gh_user['id']}@github.noemail"

    db = SessionLocal()
    try:
        provider_id = str(gh_user["id"])
        user = db.query(User).filter_by(provider="github", provider_id=provider_id).first()
        if user is None:
            user = User(
                email=email,
                username=gh_user.get("login", ""),
                provider="github",
                provider_id=provider_id,
                role="user",
                verified=True,
            )
            db.add(user)
            db.commit()
            db.refresh(user)
        cookie = create_session_cookie(user.id)
    finally:
        db.close()

    response = RedirectResponse(url="/")
    response.set_cookie(COOKIE_NAME, cookie, httponly=True, samesite="lax")
    return response


@router.get("/auth/me")
def me(request: Request):
    user = get_current_user(request)
    if user is None:
        return JSONResponse({"user": None})
    return {"id": user.id, "username": user.username, "email": user.email,
            "role": user.role, "verified": user.verified}


@router.post("/auth/logout")
def logout():
    response = JSONResponse({"ok": True})
    response.delete_cookie(COOKIE_NAME)
    return response
