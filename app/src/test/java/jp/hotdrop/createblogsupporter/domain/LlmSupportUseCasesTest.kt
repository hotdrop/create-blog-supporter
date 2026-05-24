package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import jp.hotdrop.createblogsupporter.domain.usecase.CheckSectionProofreadingUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionImprovementSuggestionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionSummaryUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateTitleProposalsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmSupportUseCasesTest {
    @Test
    fun generateTitleProposals_returnsThreeTitlesWithTopic() {
        val result = GenerateTitleProposalsUseCase()(
            TitleProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.title.contains("Compose Navigation") })
    }

    @Test
    fun generateTitleProposals_usesSafeDefaultTopicWhenTopicIsBlank() {
        val result = GenerateTitleProposalsUseCase()(
            TitleProposalRequest(
                topic = "   ",
                detail = "まだ題材が固まっていない",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.title.contains("テックブログ") })
    }

    @Test
    fun generateOutlineProposals_returnsThreeOutlines() {
        val result = GenerateOutlineProposalsUseCase()(
            OutlineProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.headings.size == 4 })
    }

    @Test
    fun generateSectionSummary_returnsSuggestionWithoutChangingSectionContent() {
        val section = section(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        val result = GenerateSectionSummaryUseCase()(
            SectionSummaryRequest(
                articleTitle = "記事タイトル",
                sectionHeading = section.heading,
                savedContent = section.content,
                draftContent = section.draftContent,
            ),
        ).successValue()

        assertTrue(result.summary.contains(section.heading))
        assertEquals("保存済み本文", section.content)
        assertEquals("編集中本文", section.draftContent)
    }

    @Test
    fun generateSectionImprovementSuggestions_returnsMultipleSuggestionsWithoutChangingSectionContent() {
        val section = section(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        val result = GenerateSectionImprovementSuggestionsUseCase()(
            SectionImprovementRequest(
                articleTitle = "記事タイトル",
                sectionHeading = section.heading,
                savedContent = section.content,
                draftContent = section.draftContent,
            ),
        ).successValue()

        assertTrue(result.size >= 2)
        assertEquals("保存済み本文", section.content)
        assertEquals("編集中本文", section.draftContent)
    }

    @Test
    fun checkSectionProofreading_returnsReferenceIssuesAndDoesNotApproveSection() {
        val section = section(
            content = "保存済み本文",
            draftContent = "LiteRM の表記を確認する",
            userApproved = false,
        )

        val result = CheckSectionProofreadingUseCase()(
            ProofreadingRequest(
                articleTitle = "記事タイトル",
                sectionHeading = section.heading,
                savedContent = section.content,
                draftContent = section.draftContent,
            ),
        ).successValue()

        assertEquals(1, result.issues.size)
        assertEquals("LiteRT-LM", result.issues.single().suggestion)
        assertFalse(section.userApproved)
    }

    private fun section(
        content: String,
        draftContent: String,
        userApproved: Boolean = false,
    ): ArticleSection =
        ArticleSection(
            id = 1,
            articleId = 1,
            heading = "設計方針",
            orderIndex = 0,
            content = content,
            draftContent = draftContent,
            proofreadStatus = ProofreadStatus.Unchecked,
            proofreadMessage = null,
            userApproved = userApproved,
            createdAt = 1,
            updatedAt = 1,
            lastSavedAt = null,
            draftUpdatedAt = null,
        )
}

private fun <T> LlmSupportResult<T>.successValue(): T =
    when (this) {
        is LlmSupportResult.Success -> value
        is LlmSupportResult.Failure -> error("Expected success but was $reason")
    }
