package com.cardra.server.exception

class ResearchJobNotFoundException(jobId: String) : RuntimeException("research job not found: $jobId")
