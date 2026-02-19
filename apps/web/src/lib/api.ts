import { api } from './apiClient'
import type {
  ApiErrorShape,
  CardResponse,
  CreateCardRequest,
  RecommendKeywordRequest,
  RecommendKeywordResponse,
  ResearchJobCreateRequest,
  ResearchJobCreateResponse,
  ResearchJobResultResponse,
  ResearchJobStatusResponse,
  ResearchRunRequest,
} from './types'

export const cardApi = {
  generate: (body: CreateCardRequest) => api.post<CardResponse>('/cards/generate', body),
  getCard: (id: string) => api.get<CardResponse>(`/cards/${id}`),
}

export const researchApi = {
  run: (body: ResearchRunRequest) => api.post<CardResponse>('/research/run', body),
  createJob: (body: ResearchJobCreateRequest) =>
    api.post<ResearchJobCreateResponse>('/research/jobs', body),
  getStatus: (jobId: string) => api.get<ResearchJobStatusResponse>(`/research/jobs/${jobId}`),
  getResult: (jobId: string) => api.get<ResearchJobResultResponse>(`/research/jobs/${jobId}/result`),
  cancel: (jobId: string) => api.post(`/research/jobs/${jobId}/cancel`),
}

export const recommendApi = {
  keywords: (body: RecommendKeywordRequest) => api.post<RecommendKeywordResponse>('/recommend/keywords', body),
  events: (body: { userId: string; events: Array<{ eventType: string; keyword: string; eventTs: string }> }) =>
    api.post('/recommend/events', body),
}

export const healthApi = {
  ping: () => api.get('/health'),
}

export function parseApiError(payload: unknown): ApiErrorShape {
  return payload as ApiErrorShape
}
