package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import kotlinx.coroutines.flow.Flow

interface BlogSupportLlmClient {
    fun streamText(request: BlogSupportLlmRequest): Flow<String>
}

data class BlogSupportLlmRequest(
    val prompt: String,
    val temperature: Float = 0.4f,
)

class LlmSupportException(
    val failure: LlmSupportFailure,
    cause: Throwable? = null,
) : RuntimeException(failure.name, cause)
