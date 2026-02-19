import type { ApiError } from './apiClient'

export type AppError = {
  code: string
  message: string
  retryable: boolean
  retryAfter?: number
  traceId?: string
  status?: number
}

export function toAppError(error: unknown): AppError {
  const anyErr = error as Partial<ApiError & { status?: number }>
  if (anyErr && (anyErr as any).code) {
    return {
      code: String(anyErr.code),
      message: String(anyErr.message ?? '요청 처리에 실패했습니다.'),
      retryable: Boolean(anyErr.retryable),
      retryAfter: anyErr.retryAfter,
      traceId: anyErr.traceId,
      status: anyErr.status,
    }
  }

  return {
    code: 'UNKNOWN',
    message: '네트워크 오류가 발생했거나 서버 응답이 비정상입니다.',
    retryable: true,
  }
}
