package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.validation.ArticleValidation
import javax.inject.Inject

class CreatePhase1ArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(topic: String, detail: String): CreatePhase1ArticleResult {
        val normalizedTopic = ArticleValidation.normalizeTopic(topic)
        if (!ArticleValidation.isValidTopic(normalizedTopic)) {
            return CreatePhase1ArticleResult.InvalidTopic
        }
        val articleId = articleRepository.createPhase1Article(
            topic = normalizedTopic,
            detail = ArticleValidation.normalizeDetail(detail),
            nowMillis = System.currentTimeMillis(),
        )
        return CreatePhase1ArticleResult.Created(articleId)
    }
}

sealed interface CreatePhase1ArticleResult {
    data class Created(val articleId: Long) : CreatePhase1ArticleResult
    data object InvalidTopic : CreatePhase1ArticleResult
}
