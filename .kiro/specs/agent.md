# Agent Guide

## Run Commands

Backend:

- install deps: pip install -r requirements.txt
- run: uvicorn main:app --reload

Frontend:

- install deps: npm install
- dev: shadow-cljs watch app
- build: shadow-cljs release app

---

## Conventions

- Backend is a thin layer
- No business logic in API routes
- Git is the source of truth
- Comments live in DB only
- Org files are never mutated outside Git commits

---

## Common Pitfalls

- Mixing business logic into FastAPI routes
- Using Flask-style patterns
- Overusing async where not needed
- Breaking relative paths in Org files
- Forgetting PythonAnywhere constraints

---

## Architecture Preferences

- Functional style where possible
- Immutable data structures
- Clear separation:

  * API
  * Services
  * Models

---

## Git Rules

- One commit per content change
- Commit messages must be meaningful
- No direct file edits without commit

---

## Frontend Rules

- State must be centralized
- Avoid side effects outside controlled flows
- Rendering must not depend on backend templates

---

## Backend Rules

- No tight coupling to FastAPI
- Services must be reusable
- API is transport only

---

## Testing Rules

- Every API endpoint must have a test
- Critical logic must be covered
- Avoid mocking Git where possible

---

## Deployment Notes

- Ensure compatibility with PythonAnywhere
- Avoid long-running tasks
- Prefer synchronous flows

---

## Mental Model

- Frontend = brain
- Backend = wire
- Git = truth

If something feels complex:
You are probably putting logic in the wrong layer.

---
