export const defaultTheme = {
  '--color-main': '#00A676',
  '--color-sub': '#E0D0C1',
  '--color-bg': '#f8faf9',
  '--color-surface': '#ffffff',
  '--color-text': '#0f172a',
  '--color-text-muted': '#334155',
  '--color-border': '#e5e7eb',
  '--card-accent': '#E0D0C1',
}

export function applyThemeVariables() {
  const root = document.documentElement
  for (const [key, value] of Object.entries(defaultTheme)) {
    root.style.setProperty(key, value)
  }
}
