package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmClient
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmRequest
import jp.hotdrop.createblogsupporter.domain.usecase.CheckSectionProofreadingUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionImprovementSuggestionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionSummaryUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateTitleProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.LlmSupportException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmSupportUseCasesTest {
    @Test
    fun generateTitleProposals_returnsThreeTitlesWithTopic() = runBlocking {
        val result = GenerateTitleProposalsUseCase(
            FakeBlogSupportLlmClient(
                "TITLE: Compose Navigation を実装から理解する\n" +
                    "TITLE: Compose Navigation の設計判断を振り返る\n" +
                    "TITLE: Compose Navigation でつまずいた点と改善策",
            ),
        )(
            TitleProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.title.contains("Compose Navigation") })
    }

    @Test
    fun generateTitleProposals_returnsFormatFailureWhenResponseCannotBeParsed() = runBlocking {
        val result = GenerateTitleProposalsUseCase(FakeBlogSupportLlmClient("タイトル案です"))(
            TitleProposalRequest(
                topic = "   ",
                detail = "まだ題材が固まっていない",
            ),
        )

        assertEquals(LlmSupportFailure.ResponseFormatInvalid, result.failureReason())
    }

    @Test
    fun generateTitleProposals_convertsClientFailure() = runBlocking {
        val result = GenerateTitleProposalsUseCase(
            FakeBlogSupportLlmClient(failure = LlmSupportFailure.ModelFileMissing),
        )(
            TitleProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        )

        assertEquals(LlmSupportFailure.ModelFileMissing, result.failureReason())
    }

    @Test(expected = CancellationException::class)
    fun generateTitleProposals_rethrowsCancellation() = runBlocking {
        GenerateTitleProposalsUseCase(FakeBlogSupportLlmClient(cancel = true))(
            TitleProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        )
        Unit
    }

    @Test
    fun generateOutlineProposals_returnsThreeOutlines() = runBlocking {
        val result = GenerateOutlineProposalsUseCase(
            FakeBlogSupportLlmClient(
                """
                OUTLINE: 実装手順から整理する構成
                - 背景と解決したかったこと
                - 最初に決めた設計方針
                - 実装で詰まったポイント
                - 次に同じことをするなら
                OUTLINE: 読者の疑問に答える構成
                - なぜこのテーマを扱うのか
                - 前提知識と全体像
                - 具体例で見る実装の流れ
                - 運用して分かった注意点
                OUTLINE: 失敗と改善を中心にした構成
                - 当初の課題
                - うまくいかなかった実装
                - 改善した設計
                - 得られた学び
                """.trimIndent(),
            ),
        )(
            OutlineProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.headings.size == 4 })
    }

    @Test
    fun generateSectionSummary_returnsSuggestionWithoutChangingSectionContent() = runBlocking {
        val section = section(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        val result = GenerateSectionSummaryUseCase(
            FakeBlogSupportLlmClient("設計方針で扱う論点を、背景・判断・実装メモに分けて整理する案です。"),
        )(
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
    fun generateSectionImprovementSuggestions_returnsMultipleSuggestionsWithoutChangingSectionContent() = runBlocking {
        val section = section(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        val result = GenerateSectionImprovementSuggestionsUseCase(
            FakeBlogSupportLlmClient(
                """
                SUGGESTION: 背景を先に置く
                DETAIL: 読者が前提を追いやすいように、実装前の課題を冒頭で短く説明する案です。
                SUGGESTION: 判断理由を明示する
                DETAIL: 採用した設計だけでなく、選ばなかった選択肢と理由も一文で補う案です。
                """.trimIndent(),
            ),
        )(
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
    fun checkSectionProofreading_returnsReferenceIssuesAndDoesNotApproveSection() = runBlocking {
        val section = section(
            content = "保存済み本文",
            draftContent = "LiteRM の表記を確認する",
            userApproved = false,
        )

        val result = CheckSectionProofreadingUseCase(
            FakeBlogSupportLlmClient(
                """
                MESSAGE: 誤字脱字・表記ゆれの候補が 1 件あります。必要なものだけ反映してください。
                ISSUE: LiteRM
                SUGGEST: LiteRT-LM
                REASON: SDK名の表記をプロジェクト内の正式表記にそろえる候補です。
                """.trimIndent(),
            ),
        )(
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

private fun <T> LlmSupportResult<T>.failureReason(): LlmSupportFailure =
    when (this) {
        is LlmSupportResult.Success -> error("Expected failure but was $value")
        is LlmSupportResult.Failure -> reason
    }

private class FakeBlogSupportLlmClient(
    private val response: String = "",
    private val failure: LlmSupportFailure? = null,
    private val cancel: Boolean = false,
) : BlogSupportLlmClient {
    override fun streamText(request: BlogSupportLlmRequest): Flow<String> {
        if (cancel) {
            return flow { throw CancellationException("cancelled") }
        }
        failure?.let { reason ->
            return flow { throw LlmSupportException(reason) }
        }
        return flowOf(response)
    }
}
