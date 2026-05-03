from unittest.mock import patch, MagicMock
from models.user import User
from services.auth_service import create_session_cookie, COOKIE_NAME


def test_me_unauthenticated(client):
    resp = client.get("/auth/me")
    assert resp.status_code == 200
    assert resp.json()["user"] is None


def test_me_authenticated(client, db):
    user = User(email="u@u.com", username="uname", provider="github", provider_id="42", role="user", verified=True)
    db.add(user)
    db.commit()
    db.refresh(user)
    cookie = create_session_cookie(user.id)

    def patched_get(request):
        token = request.cookies.get(COOKIE_NAME)
        if not token:
            return None
        from itsdangerous import URLSafeSerializer, BadSignature
        import os
        signer = URLSafeSerializer(os.getenv("SECRET_KEY", "dev-secret"), salt="session")
        try:
            data = signer.loads(token)
        except BadSignature:
            return None
        return db.get(User, data["user_id"])

    with patch("api.auth.get_current_user", patched_get):
        resp = client.get("/auth/me", cookies={COOKIE_NAME: cookie})
    assert resp.status_code == 200
    data = resp.json()
    assert data["username"] == "uname"
    assert data["role"] == "user"


def test_login_redirects(client):
    resp = client.get("/auth/login", follow_redirects=False)
    assert resp.status_code in (302, 307)
    assert "github.com/login/oauth/authorize" in resp.headers["location"]


def test_callback_creates_user(client, db):
    gh_user_data = {"id": 99, "login": "ghuser", "email": "gh@gh.com"}
    token_data = {"access_token": "fake-token"}

    mock_response = MagicMock()
    mock_response.json.side_effect = [token_data, gh_user_data]

    mock_client = MagicMock()
    mock_client.__enter__ = lambda s: mock_client
    mock_client.__exit__ = MagicMock(return_value=False)
    mock_client.post.return_value = MagicMock(json=MagicMock(return_value=token_data))
    mock_client.get.return_value = MagicMock(json=MagicMock(return_value=gh_user_data))

    with (
        patch("api.auth.httpx.Client", return_value=mock_client),
        patch("api.auth.SessionLocal", return_value=db),
    ):
        resp = client.get("/auth/callback?code=testcode", follow_redirects=False)

    assert resp.status_code in (302, 307)
    assert COOKIE_NAME in resp.cookies

    user = db.query(User).filter_by(provider_id="99").first()
    assert user is not None
    assert user.username == "ghuser"
