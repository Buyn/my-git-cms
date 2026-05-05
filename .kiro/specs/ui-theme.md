# UI Theme System

## Goal

Provide flexible, switchable UI themes without changing components.

---

## Strategy

Use:

- Tailwind utilities
- CSS variables
- Root-level classes

---

## Root Classes

- theme-starfleet
- theme-cyberpunk
- theme-minimal
- dark

Example:

<html class="theme-cyberpunk dark">

---

## Token Mapping

Tailwind must map tokens:

bg-base → var(--bg-base)
text-primary → var(--text-primary)

---

## Theme Definitions

### Starfleet

Light:
- bg: #f5f7fa
- text: #0b1a2b
- accent: #5ab4ff

Dark:
- bg: #0b1220
- text: #d6e6ff
- accent: #7fd1ff

---

### Cyberpunk

Light:
- bg: #0f0f12
- text: #e6e6e6
- accent: #ff2bd6

Dark:
- bg: #070709
- text: #ffffff
- accent: #ff2bd6

Glow:
- subtle only
- never full neon backgrounds

---

### Minimal

Light:
- bg: #ffffff
- text: #111111

Dark:
- bg: #111111
- text: #eeeeee

No accent color emphasis

---

## Rules

- No theme-specific logic in components
- No direct color usage
- Everything via tokens

---

## Toggle Behavior

- Stored in localStorage
- Applied before app init
- No flicker allowed
```

---
