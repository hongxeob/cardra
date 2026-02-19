import { api } from './apiClient'
import type {
  ApiErrorShape,
  CardResponse,
  CreateCardRequest,
  ImageGenerateRequest,
  ImageGenerateResponse,
  ImageProviderStatusResponse,
  RecommendEvent,
  RecommendEventRequest,
  RecommendEventResponse,
  RecommendKeywordRequest,
  RecommendKeywordResponse,
  ResearchJobCreateRequest,
  ResearchJobCreateResponse,
  ResearchJobResultResponse,
  ResearchJobStatusResponse,
  ResearchRunRequest,
  UiContractsResponse,
} from './types'

export const cardApi = {
  generate: (body: CreateCardRequest) => api.post<CardResponse>('/cards/generate', body),
  getCard: (id: string) => api.get<CardResponse>(`/cards/${id}`),
}

export const researchApi = {
  run: (body: ResearchRunRequest) => api.post('/research/run', body),
  createJob: (body: ResearchJobCreateRequest) =>
    api.post<ResearchJobCreateResponse>('/research/jobs', body),
  getStatus: (jobId: string) => api.get<ResearchJobStatusResponse>(`/research/jobs/${jobId}`),
  getResult: (jobId: string) => api.get<ResearchJobResultResponse>(`/research/jobs/${jobId}/result`),
  cancel: (jobId: string) => api.post(`/research/jobs/${jobId}/cancel`),
}

export const recommendApi = {
  keywords: (body: RecommendKeywordRequest) => api.post<RecommendKeywordResponse>('/recommend/keywords', body),
  events: (body: RecommendEventRequest) => api.post<RecommendEventResponse>('/recommend/events', body),
}

export const healthApi = {
  ping: () => api.get('/health'),
}

export const uiApi = {
  contracts: () => api.get<UiContractsResponse>('/ui/contracts'),
}

export const imageApi = {
  generate: (body: ImageGenerateRequest) => api.post<ImageGenerateResponse>('/images/generate', body),
  providerStatus: () => api.get<ImageProviderStatusResponse>('/images/providers/status'),
}

export function parseApiError(payload: unknown): ApiErrorShape {
  return payload as ApiErrorShape
}

export const createRecommendEvent = (keyword: string): RecommendEvent => ({
  eventType: 'search',
  keyword,
  eventTs: new Date().toISOString(),
  metadata: { source: 'web-ui' },
})
