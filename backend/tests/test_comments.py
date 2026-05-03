from unittest.mock import patch
from models.user import User
from models.comment import Comment
from services.auth_service import create_session_cookie, COOKIE_NAME


def _make_user(db, role="user"):
    u = User(email=f"{role}@test.com", username=role, provider="github", provider_id=role, role=role, verified=True)
    db.add(u)
    db.commit()
    db.refresh(u)
    return u


def _auth_client(client, db, role="user"):
    user = _make_user(db, role)
    cookie = create_session_cookie(user.id)

    # Patch auth_service to use the test db session
    original_get = __import__("services.auth_service", fromlist=["get_current_user"]).get_current_user

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

    return user, cookie, patched_get


def test_list_comments_empty(client):
    resp = client.get("/comments/some/page")
    assert resp.status_code == 200
    assert resp.json() == []


def test_create_comment_anon(client):
    resp = client.post("/comments", json={"page_path": "test", "content": "hi", "anon_email": "x@x.com"})
    assert resp.status_code == 201
    assert "id" in resp.json()


def test_create_comment_anon_no_email_fails(client):
    resp = client.post("/comments", json={"page_path": "test", "content": "hi"})
    assert resp.status_code == 400


def test_create_and_list_comment(client, db):
    client.post("/comments", json={"page_path": "p1", "content": "hello", "anon_email": "a@b.com"})
    resp = client.get("/comments/p1")
    assert len(resp.json()) == 1
    assert resp.json()[0]["content"] == "hello"


def test_update_comment_own(client, db):
    user, cookie, patched_get = _auth_client(client, db)
    comment = Comment(author_id=user.id, content="orig", page_path="p")
    db.add(comment)
    db.commit()
    db.refresh(comment)

    with patch("api.comments.get_current_user", patched_get):
        resp = client.put(f"/comments/{comment.id}", json={"content": "updated"}, cookies={COOKIE_NAME: cookie})
    assert resp.status_code == 200


def test_update_comment_other_user_forbidden(client, db):
    owner = _make_user(db, "user")
    comment = Comment(author_id=owner.id, content="orig", page_path="p")
    db.add(comment)
    db.commit()
    db.refresh(comment)

    other, cookie, patched_get = _auth_client(client, db, "user2")
    # create a second user manually
    other2 = User(email="other@test.com", username="other", provider="github", provider_id="other99", role="user", verified=True)
    db.add(other2)
    db.commit()
    db.refresh(other2)
    cookie2 = create_session_cookie(other2.id)

    def patched_get2(request):
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

    with patch("api.comments.get_current_user", patched_get2):
        resp = client.put(f"/comments/{comment.id}", json={"content": "hack"}, cookies={COOKIE_NAME: cookie2})
    assert resp.status_code == 403


def test_delete_comment(client, db):
    user, cookie, patched_get = _auth_client(client, db)
    comment = Comment(author_id=user.id, content="bye", page_path="p")
    db.add(comment)
    db.commit()
    db.refresh(comment)

    with patch("api.comments.get_current_user", patched_get):
        resp = client.delete(f"/comments/{comment.id}", cookies={COOKIE_NAME: cookie})
    assert resp.status_code == 204


def test_add_reaction(client, db):
    user, cookie, patched_get = _auth_client(client, db)
    comment = Comment(author_id=user.id, content="react", page_path="p")
    db.add(comment)
    db.commit()
    db.refresh(comment)

    with patch("api.comments.get_current_user", patched_get):
        resp = client.post(f"/comments/{comment.id}/reaction", json={"type": "👍"}, cookies={COOKIE_NAME: cookie})
    assert resp.status_code == 201
    assert "id" in resp.json()
