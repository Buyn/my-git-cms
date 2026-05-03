import os
from pathlib import Path
from unittest.mock import patch


DOCS = Path(os.environ.get("DOCS_PATH", "/tmp/test_docs"))


def _write_org(name: str, text: str):
    DOCS.mkdir(parents=True, exist_ok=True)
    (DOCS / name).write_text(text)


def test_get_content_returns_raw_and_html(client):
    _write_org("hello.org", "* Hello\nWorld")
    with patch("services.git_service.DOCS_PATH", DOCS):
        resp = client.get("/content/hello.org")
    assert resp.status_code == 200
    data = resp.json()
    assert "raw" in data and "html" in data
    assert "Hello" in data["raw"]


def test_get_content_not_found(client):
    with patch("services.git_service.DOCS_PATH", DOCS):
        resp = client.get("/content/missing.org")
    assert resp.status_code == 404


def test_put_content_requires_admin(client):
    resp = client.put("/content/hello.org", json={"content": "* Hi", "message": "test"})
    assert resp.status_code == 403


def test_put_content_as_admin(client, db):
    from models.user import User
    from services.auth_service import create_session_cookie, COOKIE_NAME

    admin = User(email="a@a.com", username="admin", provider="github", provider_id="1", role="admin", verified=True)
    db.add(admin)
    db.commit()
    db.refresh(admin)
    cookie = create_session_cookie(admin.id)

    with (
        patch("services.git_service.DOCS_PATH", DOCS),
        patch("services.git_service.write_and_commit") as mock_commit,
        patch("services.auth_service.SessionLocal") as mock_sl,
    ):
        mock_sl.return_value.__enter__ = lambda s: db
        mock_sl.return_value.__exit__ = lambda *a: None
        mock_sl.return_value.get = lambda model, pk: db.get(model, pk)
        mock_commit.return_value = None

        resp = client.put(
            "/content/new.org",
            json={"content": "* New", "message": "add"},
            cookies={COOKIE_NAME: cookie},
        )
    assert resp.status_code == 200
