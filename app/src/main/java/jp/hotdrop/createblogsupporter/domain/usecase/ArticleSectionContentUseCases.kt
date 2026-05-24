package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import javax.inject.Inject

class UpdateArticleSectionDraftContentUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
        draftContent: String,
    ): ArticleSectionContentOperationResult =
        if (
            articleRepository.updateArticleSectionDraftContent(
                articleId = articleId,
                sectionId = sectionId,
                draftContent = draftContent,
                nowMillis = System.currentTimeMillis(),
            )
        ) {
            ArticleSectionContentOperationResult.Updated
        } else {
            ArticleSectionContentOperationResult.NotPhase2OrMissing
        }
}

class SaveArticleSectionContentUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
    ): ArticleSectionContentOperationResult =
        if (
            articleRepository.saveArticleSectionContent(
                articleId = articleId,
                sectionId = sectionId,
                nowMillis = System.currentTimeMillis(),
            )
        ) {
            ArticleSectionContentOperationResult.Updated
        } else {
            ArticleSectionContentOperationResult.NotPhase2OrMissing
        }
}

class ResetArticleSectionDraftToSavedUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
    ): ArticleSectionContentOperationResult =
        if (
            articleRepository.resetArticleSectionDraftToSaved(
                articleId = articleId,
                sectionId = sectionId,
                nowMillis = System.currentTimeMillis(),
            )
        ) {
            ArticleSectionContentOperationResult.Updated
        } else {
            ArticleSectionContentOperationResult.NotPhase2OrMissing
        }
}

class UpdateArticleSectionUserApprovedUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
        userApproved: Boolean,
    ): ArticleSectionContentOperationResult =
        if (
            articleRepository.updateArticleSectionUserApproved(
                articleId = articleId,
                sectionId = sectionId,
                userApproved = userApproved,
                nowMillis = System.currentTimeMillis(),
            )
        ) {
            ArticleSectionContentOperationResult.Updated
        } else {
            ArticleSectionContentOperationResult.NotPhase2OrMissing
        }
}

sealed interface ArticleSectionContentOperationResult {
    data object Updated : ArticleSectionContentOperationResult
    data object NotPhase2OrMissing : ArticleSectionContentOperationResult
}
