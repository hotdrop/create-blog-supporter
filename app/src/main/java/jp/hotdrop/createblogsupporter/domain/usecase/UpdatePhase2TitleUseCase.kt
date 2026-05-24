package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import javax.inject.Inject

class UpdatePhase2TitleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(articleId: Long, title: String): UpdatePhase2TitleResult {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) {
            return UpdatePhase2TitleResult.InvalidTitle
        }
        val updated = articleRepository.updatePhase2Title(
            articleId = articleId,
            title = normalizedTitle,
            nowMillis = System.currentTimeMillis(),
        )
        return if (updated) {
            UpdatePhase2TitleResult.Updated
        } else {
            UpdatePhase2TitleResult.NotPhase2OrMissing
        }
    }
}

sealed interface UpdatePhase2TitleResult {
    data object Updated : UpdatePhase2TitleResult
    data object InvalidTitle : UpdatePhase2TitleResult
    data object NotPhase2OrMissing : UpdatePhase2TitleResult
}
