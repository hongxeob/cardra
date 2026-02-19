export const defaultTheme = {
  '--color-main': '#00A676',
  '--color-sub': '#E0D0C1',
  '--color-bg': '#F8FAF9',
  '--color-surface': '#FFFFFF',
  '--color-text': '#1A1A1A',
  '--color-text-muted': '#4B5563',
  '--color-border': '#E2E8E5',
  '--card-accent': '#E0D0C1',
}

export function applyThemeVariables() {
  const root = document.documentElement
  for (const [key, value] of Object.entries(defaultTheme)) {
    root.style.setProperty(key, value)
  }
}
