package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.validation.ArticleValidation
import javax.inject.Inject

class UpdatePhase1ArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        topic: String,
        detail: String,
    ): UpdatePhase1ArticleResult {
        val normalizedTopic = ArticleValidation.normalizeTopic(topic)
        if (!ArticleValidation.isValidTopic(normalizedTopic)) {
            return UpdatePhase1ArticleResult.InvalidTopic
        }
        val updated = articleRepository.updatePhase1Article(
            articleId = articleId,
            topic = normalizedTopic,
            detail = ArticleValidation.normalizeDetail(detail),
            nowMillis = System.currentTimeMillis(),
        )
        return if (updated) {
            UpdatePhase1ArticleResult.Updated
        } else {
            UpdatePhase1ArticleResult.NotPhase1OrMissing
        }
    }
}

sealed interface UpdatePhase1ArticleResult {
    data object Updated : UpdatePhase1ArticleResult
    data object InvalidTopic : UpdatePhase1ArticleResult
    data object NotPhase1OrMissing : UpdatePhase1ArticleResult
}
