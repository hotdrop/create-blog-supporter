package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.local.DeleteArticleSectionDaoResult
import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import javax.inject.Inject

class AddArticleSectionUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(articleId: Long, heading: String): ArticleSectionOperationResult {
        val normalizedHeading = heading.trim()
        if (normalizedHeading.isEmpty()) {
            return ArticleSectionOperationResult.InvalidHeading
        }
        val added = articleRepository.addArticleSection(
            articleId = articleId,
            heading = normalizedHeading,
            nowMillis = System.currentTimeMillis(),
        )
        return if (added) {
            ArticleSectionOperationResult.Updated
        } else {
            ArticleSectionOperationResult.NotPhase2OrMissing
        }
    }
}

class UpdateArticleSectionHeadingUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
        heading: String,
    ): ArticleSectionOperationResult {
        val normalizedHeading = heading.trim()
        if (normalizedHeading.isEmpty()) {
            return ArticleSectionOperationResult.InvalidHeading
        }
        val updated = articleRepository.updateArticleSectionHeading(
            articleId = articleId,
            sectionId = sectionId,
            heading = normalizedHeading,
            nowMillis = System.currentTimeMillis(),
        )
        return if (updated) {
            ArticleSectionOperationResult.Updated
        } else {
            ArticleSectionOperationResult.NotPhase2OrMissing
        }
    }
}

class DeleteArticleSectionUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(articleId: Long, sectionId: Long): DeleteArticleSectionResult =
        when (
            articleRepository.deleteArticleSection(
                articleId = articleId,
                sectionId = sectionId,
                nowMillis = System.currentTimeMillis(),
            )
        ) {
            DeleteArticleSectionDaoResult.Deleted -> DeleteArticleSectionResult.Deleted
            DeleteArticleSectionDaoResult.LastSection -> DeleteArticleSectionResult.LastSection
            DeleteArticleSectionDaoResult.NotFoundOrNotPhase2 -> DeleteArticleSectionResult.NotPhase2OrMissing
        }
}

class MoveArticleSectionUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        sectionId: Long,
        direction: ArticleSectionMoveDirection,
    ): ArticleSectionOperationResult {
        val moved = articleRepository.moveArticleSection(
            articleId = articleId,
            sectionId = sectionId,
            direction = direction,
            nowMillis = System.currentTimeMillis(),
        )
        return if (moved) {
            ArticleSectionOperationResult.Updated
        } else {
            ArticleSectionOperationResult.NotPhase2OrMissing
        }
    }
}

sealed interface ArticleSectionOperationResult {
    data object Updated : ArticleSectionOperationResult
    data object InvalidHeading : ArticleSectionOperationResult
    data object NotPhase2OrMissing : ArticleSectionOperationResult
}

sealed interface DeleteArticleSectionResult {
    data object Deleted : DeleteArticleSectionResult
    data object LastSection : DeleteArticleSectionResult
    data object NotPhase2OrMissing : DeleteArticleSectionResult
}
