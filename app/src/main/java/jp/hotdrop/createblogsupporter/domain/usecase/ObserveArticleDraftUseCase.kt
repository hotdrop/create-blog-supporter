package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveArticleDraftUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    operator fun invoke(articleId: Long): Flow<ArticleDraft?> =
        articleRepository.observeArticleDraft(articleId)
}
