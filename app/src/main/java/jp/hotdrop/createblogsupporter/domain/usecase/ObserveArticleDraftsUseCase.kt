package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraftHeader
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraftSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveArticleDraftSummariesUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    operator fun invoke(): Flow<List<ArticleDraftSummary>> =
        articleRepository.observeArticleDraftSummaries()
}

class ObserveArticleDraftHeaderUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    operator fun invoke(articleId: Long): Flow<ArticleDraftHeader?> =
        articleRepository.observeArticleDraftHeader(articleId)
}
