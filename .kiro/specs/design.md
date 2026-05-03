# System Design

## Architecture Overview

Frontend (ClojureScript)
↓
FastAPI (thin API layer)
↓
Git repository (content)
Database (comments)

---

## Directory Structure

/backend
/api
/services
/models

/frontend
/src

/Docs
*.org

/media
assets

---

## Content Handling

### Parsing

- Use Org-mode parser (Python side initially).
- Convert to structured JSON or HTML.

### Rendering

- Prefer rendering in frontend.
- Backend may pre-process content.

---

## Git Integration

### Strategy

- Use Git CLI or library.
- Operations:

  * Read content
  * Commit changes
  * Pull updates

### Auth

- OAuth via GitHub/GitLab.
- Store tokens securely.

---

## API Design

### Content

- GET /content/{path}
- PUT /content/{path}

### Comments

- GET /comments/{page}
- POST /comments
- PUT /comments/{id}
- DELETE /comments/{id}
- POST /comments/{id}/reaction

### Auth

- /auth/login
- /auth/callback

---

## Database

Use relational DB (SQLite or PostgreSQL).

Tables:

- users
- comments
- reactions

---

## Comments Model

Comment:

- id
- author_id
- content
- page_path
- created_at
- updated_at

Reaction:

- user_id
- comment_id
- type

---

## Frontend (ClojureScript)

### Responsibilities

- Routing (based on file paths)
- Rendering Org content
- Editor UI
- API communication

### State

- Centralized (re-frame or similar)

---

## Editor

- Edit raw Org text
- Preview mode
- Save triggers Git commit

---

## Media Handling

- Stored in /media
- Referenced via relative paths
- Served via backend

---

## Auth Flow

1. User clicks login
2. Redirect to Git provider
3. Callback to backend
4. Token stored
5. Session created

---

## Deployment (PythonAnywhere)

- Use WSGI/ASGI compatible setup
- Avoid background workers
- Use simple DB setup
- Static files served via platform

---

## Testing Strategy

- Backend:

  * pytest
  * API tests

- Frontend:

  * basic rendering tests

---

## Migration Strategy

To Clojure backend:

- Keep business logic separate
- Avoid FastAPI-specific patterns
- Keep API thin

---
