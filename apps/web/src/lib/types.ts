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

export type ResearchQuery = {
  keyword: string
  language: string
  country: string
  timeRange: string
}

export type ResearchUsage = {
  providerCalls: number
  latencyMs: number
  cacheHit: boolean
}

export type ResearchError = {
  code: string
  message: string
  retryable: boolean
  retryAfter?: number
  traceId?: string
  usage?: ResearchUsage
}

export type ResearchClaim = {
  claimText: string
  verdict: string
  evidenceIds: string[]
}

export type ResearchFactcheck = {
  status: string
  confidence: number
  confidenceReasons: string[]
  claims: ResearchClaim[]
}

export type ResearchSource = {
  publisher: string
  url: string
  sourceType: string
  author?: string
}

export type ResearchTimestamps = {
  publishedAt: string
  collectedAt: string
  lastVerifiedAt: string
}

export type ResearchTrend = {
  trendScore: number
  velocity: number
  regionRank: number
}

export type ResearchItem = {
  itemId: string
  title: string
  snippet: string
  source: ResearchSource
  timestamps: ResearchTimestamps
  factcheck: ResearchFactcheck
  trend: ResearchTrend
}

export type ResearchSummary = {
  brief: string
  analystNote: string
  riskFlags: string[]
}

export type ResearchRunResponse = {
  traceId: string
  status: string
  generatedAt: string
  query: ResearchQuery
  items: ResearchItem[]
  summary: ResearchSummary
  error?: ResearchError
  usage?: ResearchUsage
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
  error?: ResearchError
}

export type ResearchJobCache = {
  hit: boolean
  ttlSec: number
}

export type ResearchJobResultResponse = {
  jobId: string
  status: string
  result?: ResearchRunResponse
  error?: ResearchError
  cache?: ResearchJobCache
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

export type RecommendKeywordResponseCandidate = {
  keyword: string
  score: number
  reasons: string[]
  source: string
}

export type RecommendKeywordResponse = {
  requestId: string
  userId: string
  candidates: RecommendKeywordResponseCandidate[]
  fallbackUsed: boolean
  fallbackReason: string
  strategy: string
  modelVersion: string
  latencyMs: number
}

export type RecommendEvent = {
  eventType: string
  keyword: string
  eventTs: string
  metadata?: Record<string, string>
}

export type RecommendEventRequest = {
  userId: string
  sessionId?: string
  events: RecommendEvent[]
}

export type RecommendEventResponse = {
  accepted: number
  failed: number
}

export type UiRouteInfo = {
  feature: string
  method: string
  path: string
  description: string
}

export type UiTheme = {
  mainColor: string
  subColor: string
  background: string
  textPrimary: string
  textSecondary: string
}

export type UiContractsResponse = {
  theme: UiTheme
  routes: UiRouteInfo[]
}
