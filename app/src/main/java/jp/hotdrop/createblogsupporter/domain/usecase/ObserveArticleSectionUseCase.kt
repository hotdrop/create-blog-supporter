package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveArticleSectionUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    operator fun invoke(articleId: Long, sectionId: Long): Flow<ArticleSection?> =
        articleRepository.observeArticleSection(articleId, sectionId)
}
