# my-git-cms

A Git-based CMS with a ClojureScript frontend and a FastAPI backend.
Git is the single source of truth for content (Org-mode files). Comments live in a SQLite database.

---

## Table of Contents

- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Repository Setup](#repository-setup)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Running the Project](#running-the-project)
- [Authentication](#authentication)
- [User Management](#user-management)
- [Content Management](#content-management)
- [Comments](#comments)
- [Theming](#theming)
- [Media](#media)
- [Testing](#testing)
- [Deployment (PythonAnywhere)](#deployment-pythonanywhere)
- [API Reference](#api-reference)

---

## Architecture

```
ClojureScript (browser)
        ↓
FastAPI (backend/main.py)
        ↓
Git repo (Docs/*.org)   +   SQLite (cms.db)
```

- **Frontend** — re-frame + reitit + TailwindCSS, served from `frontend/public/`
- **Backend** — FastAPI, thin transport layer, no business logic in routes
- **Content** — `.org` files in `Docs/`, every save is a Git commit
- **Database** — SQLite for users, comments, reactions

---

## Prerequisites

| Tool       | Version               |
|------------|-----------------------|
| Python     | 3.11+                 |
| Node.js    | 18+                   |
| Java (JDK) | 11+ (for shadow-cljs) |
| Git        | any recent            |

---

## Repository Setup

```bash
git clone https://github.com/Buyn/my-git-cms.git
cd my-git-cms
```

Configure Git identity (used for content commit authorship):

```bash
git config user.name "Your Name"
git config user.email "you@example.com"
```

---

## Backend Setup

```bash
cd backend
pip install -r requirements.txt
```

Copy the example env file and fill in your values:

```bash
cp .env.example .env
```

`.env` fields:

```ini
# Path to the Docs directory (relative to backend/ or absolute)
DOCS_PATH=../Docs

# Path to the media directory
MEDIA_PATH=../media

# Random secret for signing session cookies — generate with:
#   python -c "import secrets; print(secrets.token_hex(32))"
SECRET_KEY=change-me-to-a-random-secret

# GitHub OAuth app credentials (see Authentication section)
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# SQLite database file (relative to backend/)
DATABASE_URL=sqlite:///./cms.db

# Frontend URL — used for post-login redirect
# Change to your production domain when deploying
FRONTEND_URL=http://localhost:8080
```

The database and tables are created automatically on first run.

---

## Frontend Setup

```bash
cd frontend
npm install
```

This installs `shadow-cljs` and `tailwindcss`. ClojureScript dependencies (reagent, re-frame, reitit) are downloaded by shadow-cljs on first build.

---

## Running the Project

**Terminal 1 — backend:**

```bash
cd backend
uvicorn main:app --reload --port 8000
```

**Terminal 2 — frontend JS:**

```bash
cd frontend
npx shadow-cljs watch app
```

**Terminal 3 — frontend CSS (Tailwind watch):**

```bash
cd frontend
npm run css:watch
```

Open `http://localhost:8080` in your browser.
The frontend talks to the backend at `http://localhost:8000`.

To build the frontend for production:

```bash
cd frontend
npm run css:build
npx shadow-cljs release app
```

Output goes to `frontend/public/js/app.js` and `frontend/public/css/main.css`.

---

## Authentication

Authentication is done via **GitHub OAuth**. There is no username/password login.

### Creating a GitHub OAuth App

1. Go to https://github.com/settings/developers → **OAuth Apps** → **New OAuth App**
2. Fill in:
   - **Application name**: my-git-cms (or anything)
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8000/auth/callback`
3. Click **Register application**
4. Copy **Client ID** and generate a **Client Secret**
5. Put them in `backend/.env`:
   ```ini
   GITHUB_CLIENT_ID=Ov23li...
   GITHUB_CLIENT_SECRET=abc123...
   ```

For production, update the callback URL to your real domain, e.g.:
`https://yourname.pythonanywhere.com/auth/callback`

### Login Flow

1. User clicks **Login** in the nav bar → browser redirects to `GET /auth/login`
2. Backend redirects to GitHub OAuth
3. GitHub redirects back to `GET /auth/callback?code=...`
4. Backend exchanges the code for a token, fetches the GitHub user profile, creates or finds the user in the DB, sets a signed `session` cookie
5. User is redirected to `/`

### Logout

The frontend calls `POST /auth/logout`, which clears the session cookie server-side.

---

## User Management

### User Roles

| Role    | Description                                                                                         |
|---------|-----------------------------------------------------------------------------------------------------|
| `user`  | Default for anyone who logs in via GitHub. Can comment, edit own comments, react.                   |
| `admin` | Full access. Can create/edit content, moderate comments, manage users. Sees admin UI controls.      |

There is no `anon` role in the database — anonymous commenters are identified only by the email they provide when posting a comment.

### Becoming an Admin

There is no admin UI for role management. The first admin must be promoted directly in the database.

**Step 1 — log in via GitHub** so your user record is created automatically.

**Step 2 — promote yourself:**

```bash
cd backend
sqlite3 cms.db
```

```sql
-- Find your username
SELECT id, username, email, role FROM users;

-- Promote to admin
UPDATE users SET role = 'admin' WHERE username = 'your-github-username';

-- Verify
SELECT id, username, role FROM users;

.quit
```

After the next page load the frontend will reflect your admin role (the session is re-read on every app init via `GET /auth/me`).

### Promoting Additional Admins

Same process — log in once to create the record, then promote via SQLite:

```bash
sqlite3 backend/cms.db "UPDATE users SET role = 'admin' WHERE username = 'other-username';"
```

### Viewing All Users

```bash
sqlite3 backend/cms.db "SELECT id, username, email, role, verified, created_at FROM users;"
```

### Deleting a User

```bash
sqlite3 backend/cms.db "DELETE FROM users WHERE username = 'someuser';"
```

### Creating a User Manually (without GitHub login)

```bash
cd backend
python - <<'EOF'
from models.db import SessionLocal, Base, engine
from models.user import User
from models import comment  # noqa

Base.metadata.create_all(bind=engine)
db = SessionLocal()
u = User(
    email="test@example.com",
    username="testuser",
    provider="manual",
    provider_id="manual-testuser",
    role="user",       # or "admin"
    verified=True,
)
db.add(u)
db.commit()
print(f"Created user id={u.id}")
db.close()
EOF
```

---

## Content Management

### How Content Works

- All content is stored as `.org` files in the `Docs/` directory
- Every save from the editor creates a Git commit
- The backend reads files from disk and parses Org → HTML via `orgparse`
- Internal links between posts use relative Org-mode link syntax: `[[file:other-post.org][Link text]]`

### Adding a New Post

**Option 1 — via the UI (admin only):**

1. Log in as admin
2. Click **+ New Post** in the nav bar
3. Enter a filename (e.g. `my-new-post.org`), write content, set a commit message
4. Click **Create** — the backend writes the file and commits it to Git

**Option 2 — directly via Git:**

```bash
cat > Docs/my-new-post.org <<'EOF'
* My New Post

This is the content.

** Section

Some text with a [[file:other-post.org][link to another post]].
EOF

git add Docs/my-new-post.org
git commit -m "Add my-new-post"
```

### Editing a Post

**Via the UI (admin only):**

1. Open any post
2. Click **Edit** — the page switches into inline editor mode
3. Edit the raw Org text; toggle **Preview** to see rendered output
4. Enter a commit message and click **Save**

**Via Git directly:**

```bash
vim Docs/my-post.org
git add Docs/my-post.org
git commit -m "Update my-post"
```

### Org File Format

```org
* Post Title

Introductory paragraph.

** Section Heading

Body text. Internal link: [[file:other.org][Other Post]]

External link: [[https://example.com][Example]]

#+CAPTION: Alt text
[[file:../media/image.png]]
```

---

## Comments

Comments are stored in SQLite, not Git.

### Anonymous Comment

Any visitor can comment by providing an email (no verification):

```bash
curl -X POST http://localhost:8000/comments \
  -H "Content-Type: application/json" \
  -d '{"page_path": "my-post.org", "content": "Great post!", "anon_email": "visitor@example.com"}'
```

### Authenticated Comment

Logged-in users comment without providing an email (session cookie is used automatically by the browser).

### Editing a Comment

Only the comment's author can edit it. Click **Edit** on the comment in the UI, or via API:

```bash
curl -X PUT http://localhost:8000/comments/42 \
  -H "Content-Type: application/json" \
  -b "session=<your-cookie>" \
  -d '{"content": "Updated text"}'
```

### Deleting a Comment

Owner or admin can delete via the UI (× button), or:

```bash
curl -X DELETE http://localhost:8000/comments/42 \
  -b "session=<your-cookie>"
```

### Reactions

Logged-in users can react to comments (👍 ❤️ 🚀 👀). Reacting again on the same comment updates the reaction type.

```bash
curl -X POST http://localhost:8000/comments/42/reaction \
  -H "Content-Type: application/json" \
  -b "session=<your-cookie>" \
  -d '{"type": "👍"}'
```

---

## Theming

The frontend supports three themes, each with light and dark modes:

| Theme       | Character                          |
|-------------|------------------------------------|
| `starfleet` | Clean, soft blue accents (default) |
| `cyberpunk` | Dark-first, neon pink accents      |
| `minimal`   | Near-monochrome, typography-first  |

The theme dropdown and light/dark toggle are in the nav bar. Selection is persisted in `localStorage` and applied before the first render (no flash).

Themes are implemented via CSS variables on the `<html>` element:

```html
<html class="theme-cyberpunk dark">
```

All component styles use semantic tokens (`text-accent`, `bg-surface`, `border-DEFAULT`, etc.) — never raw colors.

---

## Media

Place media files in the `media/` directory at the project root:

```bash
cp ~/my-image.png media/my-image.png
```

Reference in Org files:

```org
#+CAPTION: My image
[[file:../media/my-image.png]]
```

The backend serves `media/` as static files at `/media/`.

---

## Testing

```bash
cd backend
pytest tests/ -v
```

All tests use an in-memory SQLite database and do not touch the real `cms.db` or `Docs/`.

---

## Deployment (PythonAnywhere)

### 1. Upload the project

```bash
git clone https://github.com/Buyn/my-git-cms.git
```

### 2. Install dependencies

```bash
cd my-git-cms/backend
pip install --user -r requirements.txt
```

### 3. Configure the WSGI file

```python
import sys
sys.path.insert(0, '/home/yourusername/my-git-cms/backend')

from main import app as application
```

### 4. Set environment variables

In **Web → Environment variables**:

```
SECRET_KEY=your-secret
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
DOCS_PATH=/home/yourusername/my-git-cms/Docs
MEDIA_PATH=/home/yourusername/my-git-cms/media
DATABASE_URL=sqlite:////home/yourusername/my-git-cms/backend/cms.db
```

### 5. Static files

| URL       | Directory                                       |
|-----------|-------------------------------------------------|
| `/media/` | `/home/yourusername/my-git-cms/media`           |
| `/`       | `/home/yourusername/my-git-cms/frontend/public` |

### 6. Build the frontend

```bash
cd my-git-cms/frontend
npm install
npm run css:build
npx shadow-cljs release app
```

### 7. Update the GitHub OAuth callback URL

Set callback to `https://yourusername.pythonanywhere.com/auth/callback` and update CORS in `backend/main.py`:

```python
allow_origins=["https://yourusername.pythonanywhere.com"],
```

### 8. Reload the web app

Click **Reload** in the PythonAnywhere Web tab.

---

## API Reference

### Content

| Method | Path                | Auth  | Description                                          |
|--------|---------------------|-------|------------------------------------------------------|
| GET    | `/content/list`     | —     | List all post filenames                              |
| GET    | `/content/{path}`   | —     | Read an Org file. Returns `{raw, html}`              |
| PUT    | `/content/{path}`   | admin | Write file + Git commit. Body: `{content, message}`  |

### Comments

| Method | Path                      | Auth        | Description                                                  |
|--------|---------------------------|-------------|--------------------------------------------------------------|
| GET    | `/comments/{page_path}`   | —           | List comments for a page                                     |
| POST   | `/comments`               | —           | Create comment. Body: `{page_path, content, anon_email?}`    |
| PUT    | `/comments/{id}`          | owner       | Edit comment. Body: `{content}`                              |
| DELETE | `/comments/{id}`          | owner/admin | Delete comment                                               |
| POST   | `/comments/{id}/reaction` | user        | Add/update reaction. Body: `{type}`                          |

### Auth

| Method | Path               | Description                              |
|--------|--------------------|------------------------------------------|
| GET    | `/auth/login`      | Redirect to GitHub OAuth                 |
| GET    | `/auth/callback`   | OAuth callback, sets session cookie      |
| GET    | `/auth/me`         | Current user `{id, username, email, role, verified}` |
| POST   | `/auth/logout`     | Clear session cookie                     |

### Media

Static files served at `/media/<filename>`.
