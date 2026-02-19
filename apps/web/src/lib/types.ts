export type CardStatus = 'PROCESSING' | 'COMPLETED' | 'FAILED'

export type CardStyle = {
  tone: string
  layout: string
  emphasis: string
}

export type CardMedia = {
  imageUrl?: string
  imageType?: string
  altText?: string
}

export type CardCta = {
  label: string
  actionType: string
  target?: string
}

export type CardItem = {
  title: string
  body: string
  source: string[]
  sourceAt: string
  variant?: string
  style?: CardStyle
  media?: CardMedia
  cta?: CardCta
  tags?: string[]
  imageHint?: string
}

export type CardResponse = {
  id: string
  keyword: string
  cards: CardItem[]
  status: CardStatus
  createdAt: string
}

export type CreateCardRequest = {
  keyword: string
  tone?: string
}

export type ResearchRunRequest = {
  keyword: string
  language?: string
  country?: string
  timeRange?: string
  maxItems?: number
  summaryLevel?: string
  factcheckMode?: string
}

export type ResearchJobCreateRequest = ResearchRunRequest & { idempotencyKey?: string }

export type ResearchJobCreateResponse = {
  jobId: string
  status: string
  traceId: string
}

export type ResearchJobStatusResponse = {
  jobId: string
  status: string
  createdAt: string
  updatedAt: string
  error?: ApiErrorShape
}

export type ResearchJobResultResponse = {
  jobId: string
  status: string
  result?: unknown
  error?: ApiErrorShape
  cache?: { hit: boolean; ttlSec: number }
}

export type ApiErrorShape = {
  code: string
  message: string
  retryable: boolean
  retryAfter?: number
}

export type RecommendKeywordRequest = {
  userId: string
  currentQuery?: string
  locale?: string
  categoryId?: string
  limit?: number
  excludeKeywords?: string[]
}

export type RecommendKeywordResponse = {
  requestId: string
  userId: string
  candidates: { keyword: string; score: number; reasons: string[]; source: string }[]
  fallbackUsed: boolean
  fallbackReason: string
  strategy: string
  modelVersion: string
  latencyMs: number
}
