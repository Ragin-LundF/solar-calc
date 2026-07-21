declare global {
  interface Window {
    __env?: { apiBase?: string };
  }
}

// Read from public/env.js at runtime; falls back to same-origin default.
export const API_BASE = window.__env?.apiBase ?? '/api/v1';
