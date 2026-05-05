/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.cljs", "./public/index.html"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        base:    "var(--bg-base)",
        surface: "var(--bg-surface)",
        raised:  "var(--bg-elevated)",
        primary: "var(--text-primary)",
        muted:   "var(--text-muted)",
        accent:  "var(--accent)",
        danger:  "var(--danger)",
      },
      borderColor: {
        DEFAULT: "var(--border)",
        accent:  "var(--accent)",
        danger:  "var(--danger)",
      },
      boxShadow: {
        glow: "var(--glow)",
      },
      fontFamily: {
        mono: ["'JetBrains Mono'", "monospace"],
      },
    },
  },
  plugins: [],
};
