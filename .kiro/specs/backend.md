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
