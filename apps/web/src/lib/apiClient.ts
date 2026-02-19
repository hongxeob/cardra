export type Method = 'GET' | 'POST'

export type ApiError = {
  code: string
  message: string
  retryable: boolean
  retryAfter?: number
  traceId?: string
}

const BASE_URL = '/api/v1'

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init.headers as Record<string, string>),
    },
    ...init,
  })

  const text = await res.text()
  const payload = text ? JSON.parse(text) : undefined

  if (!res.ok) {
    const err = payload as ApiError
    throw {
      code: err?.code ?? `HTTP_${res.status}`,
      message: err?.message ?? res.statusText,
      retryable: err?.retryable ?? [429, 500, 502, 503, 504].includes(res.status),
      retryAfter: err?.retryAfter,
      traceId: err?.traceId,
      status: res.status,
    }
  }

  return payload as T
}

export const api = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'POST',
      body: body ? JSON.stringify(body) : undefined,
    }),
}
