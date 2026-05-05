# API Specification

## Content

### GET /content/list

Returns list of all content file paths.

Response:
- array of strings (filenames)

---

### GET /content/{path}

Returns content for a given path.

Response:
- raw: string (Org text)
- html: string (rendered)

Errors:
- 404 if not found

---

### PUT /content/{path}

Updates content.

Body:
- content: string (Org text)
- message: string (commit message)

Rules:
- Admin only

---

## Comments

### GET /comments/{page_path}

Returns list of comments for a page.

---

### POST /comments

Creates a comment.

Body:
- content: string
- page_path: string
- email: string (required for anonymous)

---

### PUT /comments/{id}

Updates a comment.

Rules:
- Owner only

---

### DELETE /comments/{id}

Deletes a comment.

Rules:
- Owner or admin

---

### POST /comments/{id}/reaction

Adds a reaction.

Body:
- type: string

---

## Auth

### GET /auth/login

Redirects to provider

---

### GET /auth/callback

Handles OAuth callback

---

### GET /auth/me

Returns current user

Response:
- id
- email
- username
- role
- verified

---

### POST /auth/logout

Clears session cookie

---
## Note

API is completely unaware of themes or styling.

Themes are handled entirely in frontend.

---

