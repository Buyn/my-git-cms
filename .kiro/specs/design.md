# System Design

## Architecture Overview

Frontend (ClojureScript + TailwindCSS)
↓
Backend API (FastAPI - thin layer)
↓
Git (content)
Database (comments)

---

## Core Principles

- Git is the only source of truth for content
- Backend is a transport and integration layer
- Frontend owns rendering and interaction
- Clear separation between layers

---

## Data Flow

1. Frontend requests content
2. Backend reads from Git
3. Backend returns raw + processed content
4. Frontend renders and handles UI

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

## Core UI Philosophy

- UI must be theme-driven
- Styling must not be hardcoded
- All colors must come from theme tokens

---

## Theme System

Themes are implemented via:

- TailwindCSS utilities
- CSS variables (design tokens)
- Root-level class switch

Example:

<html class="theme-starfleet dark">

---

## Theme Switching

- Controlled by frontend
- Stored in localStorage
- Applied instantly (no reload)

---

## Supported Themes

- starfleet (default)
- cyberpunk
- minimal

Each theme supports:

- light mode
- dark mode

---

## Separation of Concerns

Frontend:
- Rendering
- Navigation
- Editing
- Theming
- State management
- Interaction

Backend:
- Authentication
- Git interaction
- Comment persistence
```

---
