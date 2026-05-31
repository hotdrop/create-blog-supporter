package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationSectionContext
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmClient
import jp.hotdrop.createblogsupporter.domain.usecase.BlogSupportLlmRequest
import jp.hotdrop.createblogsupporter.domain.usecase.CheckSectionProofreadingUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionImprovementSuggestionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionPastePromptUseCase
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
    fun generateTitleProposals_fallsBackWhenResponseCannotBeParsed() = runBlocking {
        val result = GenerateTitleProposalsUseCase(FakeBlogSupportLlmClient("タイトル案です"))(
            TitleProposalRequest(
                topic = "   ",
                detail = "まだ題材が固まっていない",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.title.isNotBlank() })
    }

    @Test
    fun generateTitleProposals_fallsBackWhenModelReturnsLooseText() = runBlocking {
        val result = GenerateTitleProposalsUseCase(
            FakeBlogSupportLlmClient(
                """
                1. Compose Navigation の基本を整理する
                2. Route分割で学んだ設計判断
                3. 実装して分かった注意点
                """.trimIndent(),
            ),
        )(
            TitleProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertEquals("Compose Navigation の基本を整理する", result.first().title)
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
    fun generateOutlineProposals_fallsBackWhenModelReturnsLooseText() = runBlocking {
        val result = GenerateOutlineProposalsUseCase(
            FakeBlogSupportLlmClient(
                """
                背景と課題
                設計方針
                実装の流れ
                振り返り
                """.trimIndent(),
            ),
        )(
            OutlineProposalRequest(
                topic = "Compose Navigation",
                detail = "RouteとScreenを分ける",
            ),
        ).successValue()

        assertEquals(3, result.size)
        assertTrue(result.all { it.headings.size >= 3 })
        assertEquals("背景と課題", result.first().headings.first())
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

    @Test
    fun checkSectionProofreading_returnsNoIssuesWhenOnlyMessageExists() = runBlocking {
        val result = CheckSectionProofreadingUseCase(
            FakeBlogSupportLlmClient("MESSAGE: 候補は見つかりませんでした。"),
        )(
            ProofreadingRequest(
                articleTitle = "記事タイトル",
                sectionHeading = "設計方針",
                savedContent = "",
                draftContent = "表記ゆれがない本文です。",
            ),
        ).successValue()

        assertEquals("候補は見つかりませんでした。", result.message)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun checkSectionProofreading_returnsMultipleIssues() = runBlocking {
        val result = CheckSectionProofreadingUseCase(
            FakeBlogSupportLlmClient(
                """
                MESSAGE: 候補が 2 件あります。
                ISSUE: LiteRM
                SUGGEST: LiteRT-LM
                REASON: SDK名の表記候補です。
                ISSUE: できました
                SUGGEST: できました。
                REASON: 句点を補う候補です。
                """.trimIndent(),
            ),
        )(
            ProofreadingRequest(
                articleTitle = "記事タイトル",
                sectionHeading = "設計方針",
                savedContent = "",
                draftContent = "LiteRM を設定できました",
            ),
        ).successValue()

        assertEquals(2, result.issues.size)
        assertEquals("LiteRT-LM", result.issues[0].suggestion)
        assertEquals("できました。", result.issues[1].suggestion)
    }

    @Test
    fun checkSectionProofreading_convertsClientFailure() = runBlocking {
        val result = CheckSectionProofreadingUseCase(
            FakeBlogSupportLlmClient(failure = LlmSupportFailure.ModelNotConfigured),
        )(
            ProofreadingRequest(
                articleTitle = "記事タイトル",
                sectionHeading = "設計方針",
                savedContent = "",
                draftContent = "チェック対象本文",
            ),
        )

        assertEquals(LlmSupportFailure.ModelNotConfigured, result.failureReason())
    }

    @Test(expected = CancellationException::class)
    fun checkSectionProofreading_rethrowsCancellation() = runBlocking {
        CheckSectionProofreadingUseCase(FakeBlogSupportLlmClient(cancel = true))(
            ProofreadingRequest(
                articleTitle = "記事タイトル",
                sectionHeading = "設計方針",
                savedContent = "",
                draftContent = "チェック対象本文",
            ),
        )
        Unit
    }

    @Test
    fun generateSectionPastePrompt_returnsRequestWithArticleContextWithoutChangingSectionContent() {
        val section = section(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        val prompt = GenerateSectionPastePromptUseCase()(
            consultationRequest(section = section),
        )

        assertTrue(prompt.contains("現在の章」の完成本文案"))
        assertTrue(prompt.contains("現在の章の完成本文案を日本語で作成してください。"))
        assertFalse(prompt.contains("完成本文を代筆せず"))
        assertFalse(prompt.contains("助言してほしい"))
        assertTrue(prompt.contains("記事タイトル:"))
        assertTrue(prompt.contains("Compose Navigationの設計判断"))
        assertTrue(prompt.contains("目次構成:"))
        assertTrue(prompt.contains("1. 設計方針（現在の章）"))
        assertTrue(prompt.contains("補足要望:"))
        assertTrue(prompt.contains("この章では何を書けばいいでしょうか？"))
        assertEquals("保存済み本文", section.content)
        assertEquals("編集中本文", section.draftContent)
    }

    @Test
    fun generateSectionPastePrompt_truncatesOtherSectionBodyButKeepsTargetSectionBody() {
        val targetDraft = "現在章の本文".repeat(120)
        val otherDraft = "他章の長い本文".repeat(80) + "末尾は含めない"

        val prompt = GenerateSectionPastePromptUseCase()(
            consultationRequest(
                section = section(content = "保存済み本文", draftContent = targetDraft),
                outlineContext = listOf(
                    sectionContext(
                        orderIndex = 0,
                        heading = "設計方針",
                        savedContent = "保存済み本文",
                        draftContent = targetDraft,
                        isTarget = true,
                    ),
                    sectionContext(
                        orderIndex = 1,
                        heading = "実装メモ",
                        savedContent = "",
                        draftContent = otherDraft,
                    ),
                ),
            ),
        )

        assertTrue(prompt.contains(targetDraft.take(2400)))
        assertTrue(prompt.contains(otherDraft.take(140)))
        assertFalse(prompt.contains("末尾は含めない"))
    }

    @Test
    fun generateSectionPastePrompt_returnsRequestWhenUserQuestionIsBlank() {
        val prompt = GenerateSectionPastePromptUseCase()(
            consultationRequest(userQuestion = "   "),
        )

        assertTrue(prompt.contains("補足要望:"))
        assertTrue(prompt.contains("未入力"))
        assertTrue(prompt.contains("現在の章の完成本文案を日本語で作成してください。"))
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

    private fun consultationRequest(
        section: ArticleSection = section(content = "保存済み本文", draftContent = "編集中本文"),
        outlineContext: List<SectionConsultationSectionContext> = listOf(
            sectionContext(
                orderIndex = 0,
                heading = "設計方針",
                savedContent = "保存済み本文",
                draftContent = "編集中本文",
                isTarget = true,
            ),
            sectionContext(
                orderIndex = 1,
                heading = "実装メモ",
                savedContent = "保存済みの他章",
                draftContent = "",
            ),
        ),
        userQuestion: String = "この章では何を書けばいいでしょうか？",
    ): SectionConsultationRequest =
        SectionConsultationRequest(
            articleTitle = "Compose Navigationの設計判断",
            topic = "Navigation Compose",
            detail = "RouteとScreenを分ける",
            targetSection = sectionContext(
                orderIndex = section.orderIndex,
                heading = section.heading,
                savedContent = section.content,
                draftContent = section.draftContent,
                isTarget = true,
            ),
            outlineContext = outlineContext,
            userQuestion = userQuestion,
        )

    private fun sectionContext(
        orderIndex: Int,
        heading: String,
        savedContent: String,
        draftContent: String,
        isTarget: Boolean = false,
    ): SectionConsultationSectionContext =
        SectionConsultationSectionContext(
            orderIndex = orderIndex,
            heading = heading,
            savedContent = savedContent,
            draftContent = draftContent,
            isTarget = isTarget,
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
    val requests = mutableListOf<BlogSupportLlmRequest>()

    override fun streamText(request: BlogSupportLlmRequest): Flow<String> {
        requests += request
        if (cancel) {
            return flow { throw CancellationException("cancelled") }
        }
        failure?.let { reason ->
            return flow { throw LlmSupportException(reason) }
        }
        return flowOf(response)
    }
}
