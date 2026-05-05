# Backend Specification

## Responsibilities

- Authentication
- Git operations
- Comments persistence

---

## Content Handling

- Returns raw + optional HTML
- No styling responsibility

---

## Git Integration

Operations:
- Read files
- Write files
- Commit changes

Rules:
- One commit per content change
- Meaningful commit messages

---

## Database

Tables:
- users
- comments
- reactions

---

## Auth

- OAuth-based (GitHub/GitLab)
- Session-based authentication
- User roles enforced at API layer

---

## Comments Logic

- Linked to page_path
- Ownership enforced
- Reactions linked to users

---

## Profile Management

Users must be able to:

- Update their display name
- Request email change
- View their role

---

## Email Change Flow

1. User submits new email
2. Backend:
   - validates uniqueness
   - generates verification code
   - stores pending_email
3. User verifies email
4. Email is updated

Rules:
- Old email remains active until verification
- Unverified email must not replace current one
```

---

## Content Creation

- Creating new content is done via PUT /content/{path}
- Backend must:
  - Create file if not exists
  - Commit change to Git

---

## Constraints

- Must work on PythonAnywhere
- No background workers
- Prefer synchronous operations

---

## Design Rules

- No business logic in API routes
- Services must be reusable
- No tight coupling to FastAPI

---
