package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveArticleDraftsUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    operator fun invoke(): Flow<List<ArticleDraft>> = articleRepository.observeArticleDrafts()
}
