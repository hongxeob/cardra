export const defaultTheme = {
  '--color-main': '#00A676',
  '--color-sub': '#E0D0C1',
  '--color-bg': '#F8FAF9',
  '--color-surface': '#FFFFFF',
  '--color-text': '#1A1A1A',
  '--color-text-muted': '#6B7280',
  '--color-border': '#E5E9E7',
}

export function applyThemeVariables(overrides: Record<string, string> = {}) {
  const root = document.documentElement

  for (const [key, value] of Object.entries({ ...defaultTheme, ...overrides })) {
    root.style.setProperty(key, value)
  }
}

export async function hydrateThemeFromServer() {
  try {
    const res = await fetch('/api/v1/ui/theme')
    if (!res.ok) {
      return
    }

    const json = (await res.json()) as {
      mainColor?: string
      subColor?: string
      background?: string
      textPrimary?: string
      textSecondary?: string
    }

    applyThemeVariables({
      '--color-main': json.mainColor || defaultTheme['--color-main'],
      '--color-sub': json.subColor || defaultTheme['--color-sub'],
      '--color-bg': json.background || defaultTheme['--color-bg'],
      '--color-text': json.textPrimary || defaultTheme['--color-text'],
      '--color-text-muted': json.textSecondary || defaultTheme['--color-text-muted'],
    })
  } catch {
    // Keep default theme if server theme endpoint is unavailable.
  }
}
