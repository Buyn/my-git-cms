# Frontend Specification

## Stack

- ClojureScript
- TailwindCSS

---


## Responsibilities

- Routing
- Rendering content
- Editing content
- State management
- API communication

---

## Routing

- Based on content path
- URL maps directly to `.org` files

---

## Rendering

- Receives raw + HTML from backend
- Responsible for displaying content
- Must support internal links

---

## Editor

- Edits raw Org text
- Provides preview mode
- Saves via API

Flow:
1. Load content
2. Edit raw text
3. Preview
4. Save (commit)

---

## State Management

- Centralized state (e.g. re-frame)
- Must not scatter state across components

---

## API Interaction

- All communication via API layer
- No direct Git interaction

---

## Styling System

### TailwindCSS

- Utility-first styling
- No custom CSS except theme variables

---

### Theme Tokens

All styling must use semantic tokens:

- bg-base
- text-primary
- text-muted
- border-default
- accent

Never use raw colors like:
- text-gray-500 ❌
- bg-black ❌

---

## Theme Architecture

Themes are defined via CSS variables:

Example:

:root {
  --bg-base: #ffffff;
  --text-primary: #0a0a0a;
}

.dark {
  --bg-base: #0a0a0a;
  --text-primary: #ffffff;
}

.theme-starfleet {
  --accent: #5ab4ff;
}

.theme-cyberpunk {
  --accent: #ff2bd6;
}

---

## Theme Switching

User can select:

- Light
- Dark
- System (optional)
- Theme variant

UI must provide:

- Toggle (light/dark)
- Dropdown (theme)

---

## Theme Behavior

- No flashing on load
- Theme applied before render
- Persisted in localStorage

---

## Visual Style

### Starfleet

- Clean
- Soft blue accents
- High readability
- Minimal glow

### Cyberpunk

- Dark-first
- Neon accents
- Subtle glow (NOT excessive)

### Minimal

- Almost monochrome
- No glow
- Focus on typography

---

## Components

All components must:

- Use tokens only
- Be theme-independent
- Avoid hardcoded styles

---

## Editor UI

- Same theme system
- No separate styling

---

## Admin Editing Experience

### Edit Existing Content

- If the current user is admin:
  - Viewing a page must provide an "Edit" action
  - Edit action switches the page into editor mode

- Editor loads:
  - raw Org content
  - preview mode

---

### Create New Content

- Admin must have access to "New Post" action in UI

Possible locations:
- Navigation bar
- Admin panel section

---

### Editor Flow

Edit existing:
1. Open page
2. Click "Edit"
3. Modify content
4. Save → API call

Create new:
1. Click "New Post"
2. Enter path/slug
3. Enter content
4. Save → API call

---

### Permissions

- Only admin can:
  - Edit content
  - Create content

- Non-admin users:
  - Must never see edit controls
```

---

## Comments UI

- Display comments per page
- Allow creation/edit/delete
- Support reactions

---
## Design Rules

- No backend templating
- No business logic duplication
- Controlled side effects only
- No inline styles
- No CSS frameworks except Tailwind
- No duplicated styles

---
