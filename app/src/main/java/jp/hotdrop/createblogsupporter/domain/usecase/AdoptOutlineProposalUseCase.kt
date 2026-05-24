package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import javax.inject.Inject

class AdoptOutlineProposalUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
) {
    suspend operator fun invoke(
        articleId: Long,
        title: String,
        headings: List<String>,
    ): AdoptOutlineProposalResult {
        if (title.isBlank() || headings.isEmpty() || headings.any { it.isBlank() }) {
            return AdoptOutlineProposalResult.InvalidProposal
        }
        val adopted = articleRepository.adoptOutlineProposal(
            articleId = articleId,
            title = title.trim(),
            headings = headings.map { it.trim() },
            nowMillis = System.currentTimeMillis(),
        )
        return if (adopted) {
            AdoptOutlineProposalResult.Adopted
        } else {
            AdoptOutlineProposalResult.NotPhase1OrMissing
        }
    }
}

sealed interface AdoptOutlineProposalResult {
    data object Adopted : AdoptOutlineProposalResult
    data object InvalidProposal : AdoptOutlineProposalResult
    data object NotPhase1OrMissing : AdoptOutlineProposalResult
}
