# Project Requirements

## Overview

The system is a Git-based CMS with a ClojureScript frontend and a Python (FastAPI) backend.
Git is the single source of truth for content. The frontend acts as both renderer and editor.

The system must support content stored as Org-mode files, with relative linking and media support.

---

## Content Model

- All content is stored as `.org` files in a single directory.
- Files may contain:

  * Internal links (relative paths)
  * External links
  * Media references (images, possibly video)
- Media files are stored in a dedicated directory.

---

## Core Features

### Content Rendering

- Render Org files into HTML.
- Support internal navigation between files.
- Support embedded media.

### Editing

- Frontend provides editing capabilities.
- Changes are committed directly to Git.
- Admin users must be able to edit existing content pages.
- Admin users must be able to create new content pages.

### Git Integration

- Git repository is the source of truth.
- All content changes are persisted via commits.
- Authentication is based on Git providers (GitHub/GitLab).

---

## User Roles

### Anonymous User

- Can subscribe via email (no verification required).
- Can comment on existing posts.

### Registered User

- Email verification required.
- Can:

  * Manage own profile
  * Edit own comments
  * React to other comments

### Admin

- Full access.
- Can:

  * Create and edit content
  * Moderate comments
  * Manage users

---

## Comments System

- Stored in a database (not Git).
- Linked to content pages.
- Supports:

  * Editing (owner only)
  * Reactions

---

## Frontend

- Built with ClojureScript.
- Responsible for:

  * Rendering content
  * Navigation
  * Editing interface

---

## Backend

- Built with FastAPI.
- Responsibilities:

  * Authentication
  * Git interaction (commits, pulls)
  * Comments API

---

## Hosting Constraints

- Must be compatible with PythonAnywhere.
- Avoid long-running background workers.
- Prefer synchronous or lightweight async operations.

---

## Testing

- The project must include automated tests.
- Cover:

  * API endpoints
  * Content parsing
  * Core logic

---

## Non-Goals

- No heavy CMS UI frameworks.
- No dependency on monolithic backend systems.
- No tight coupling between frontend and backend.

---

## Design Principles

- Git-first architecture
- Backend as thin transport layer
- Frontend-driven experience
- Easy migration to alternative backend (e.g. Clojure)

---
