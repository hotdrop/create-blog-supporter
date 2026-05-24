package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalStubUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateOutlineProposalStubUseCaseTest {
    private val useCase = GenerateOutlineProposalStubUseCase()

    @Test
    fun invoke_returnsThreeTitleAndOutlineProposals() {
        val result = useCase(
            ArticleDraft(
                id = 1,
                phase = ArticlePhase.Phase1,
                title = "",
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
                status = ArticleStatus.Draft,
                createdAt = 1,
                updatedAt = 1,
                exportedAt = null,
            ),
        )

        assertEquals(3, result.titleProposals.size)
        assertEquals(3, result.outlineProposals.size)
        assertTrue(result.titleProposals.all { it.title.contains("Compose Navigation") })
        assertTrue(result.outlineProposals.all { it.headings.size == 4 })
    }
}
