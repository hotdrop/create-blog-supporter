package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationSectionContext
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest

private const val MaxArticleTitleLength = 120
private const val MaxTopicLength = 160
private const val MaxDetailLength = 400
private const val MaxTargetSavedContentLength = 1200
private const val MaxTargetDraftContentLength = 2400
private const val MaxOtherSectionMemoLength = 140
private const val MaxUserQuestionLength = 800
private const val NearbySectionDistance = 2

internal fun buildTitlePrompt(request: TitleProposalRequest): String =
    """
    題材:
    ${request.topic.trim().ifBlank { "テックブログ" }}

    詳細メモ:
    ${request.detail.trim().ifBlank { "未入力" }}

    タイトル案を3件だけ返してください。本文の代筆は禁止です。
    出力形式:
    TITLE: タイトル案1
    TITLE: タイトル案2
    TITLE: タイトル案3
    """.trimIndent()

internal fun buildOutlinePrompt(request: OutlineProposalRequest): String =
    """
    題材:
    ${request.topic.trim().ifBlank { "テックブログ" }}

    詳細メモ:
    ${request.detail.trim().ifBlank { "未入力" }}

    目次案を3件返してください。各案は4つの章節見出しだけにしてください。本文の代筆は禁止です。
    出力形式:
    OUTLINE: 構成名
    - 見出し1
    - 見出し2
    - 見出し3
    - 見出し4
    """.trimIndent()

internal fun buildSectionSummaryPrompt(request: SectionSummaryRequest): String =
    """
    記事タイトル:
    ${request.articleTitle.ifBlank { "未設定" }}

    章節見出し:
    ${request.sectionHeading.ifBlank { "未設定" }}

    保存済み本文:
    ${request.savedContent.ifBlank { "未入力" }}

    編集中本文:
    ${request.draftContent.ifBlank { "未入力" }}

    この章節で扱う論点の概要案だけを日本語で返してください。完成本文の代筆は禁止です。
    """.trimIndent()

internal fun buildSectionPastePrompt(request: SectionConsultationRequest): String {
    val sortedSections = request.outlineContext.sortedBy { it.orderIndex }
    val targetIndex = request.targetSection.orderIndex
    val outline = sortedSections.joinToString(separator = "\n") { section ->
        val marker = if (section.orderIndex == targetIndex || section.isTarget) "（現在の章）" else ""
        "${section.orderIndex + 1}. ${section.heading.normalizePromptText()}$marker"
    }.ifBlank {
        "${request.targetSection.orderIndex + 1}. ${request.targetSection.heading.normalizePromptText()}（現在の章）"
    }
    val otherSections = sortedSections
        .filterNot { it.orderIndex == targetIndex || it.isTarget }
        .joinToString(separator = "\n") { section ->
            buildOtherSectionContextLine(section, targetIndex, sortedSections.size)
        }
        .ifBlank { "他の章はまだありません。" }

    return """
    ChatGPTへの依頼:
    以下の文脈をもとに、「現在の章」の完成本文案をテックブログ向けの自然な文章として作成してください。
    後から自分で修正する前提なので、元メモや既存文脈から外れた断定は避けてください。

    記事タイトル:
    ${request.articleTitle.normalizePromptText().ifBlank { "未設定" }.limitPromptText(MaxArticleTitleLength)}

    元メモ:
    題材: ${request.topic.normalizePromptText().ifBlank { "未入力" }.limitPromptText(MaxTopicLength)}
    詳細: ${request.detail.normalizePromptText().ifBlank { "未入力" }.limitPromptText(MaxDetailLength)}

    目次構成:
    $outline

    現在の章:
    見出し: ${request.targetSection.heading.normalizePromptText().ifBlank { "未設定" }}
    保存済み本文:
    ${request.targetSection.savedContent.normalizePromptText().ifBlank { "未入力" }.limitPromptText(MaxTargetSavedContentLength)}
    編集中本文:
    ${request.targetSection.draftContent.normalizePromptText().ifBlank { "未入力" }.limitPromptText(MaxTargetDraftContentLength)}

    他章コンテキスト:
    $otherSections

    補足要望:
    ${request.userQuestion.normalizePromptText().ifBlank { "未入力" }.limitPromptText(MaxUserQuestionLength)}

    出力してほしいもの:
    現在の章の完成本文案を日本語で作成してください。
    """.trimIndent()
}

internal fun buildSectionImprovementPrompt(request: SectionImprovementRequest): String =
    """
    記事タイトル:
    ${request.articleTitle.ifBlank { "未設定" }}

    章節見出し:
    ${request.sectionHeading.ifBlank { "未設定" }}

    保存済み本文:
    ${request.savedContent.ifBlank { "未入力" }}

    編集中本文:
    ${request.draftContent.ifBlank { "未入力" }}

    改善提案を3件まで返してください。本文の書き換えではなく、ユーザーが判断できる提案にしてください。
    出力形式:
    SUGGESTION: 提案タイトル
    DETAIL: 提案理由
    """.trimIndent()

private fun buildOtherSectionContextLine(
    section: SectionConsultationSectionContext,
    targetIndex: Int,
    sectionCount: Int,
): String {
    val content = section.draftContent.ifBlank { section.savedContent }.normalizePromptText()
    val state = when {
        section.draftContent.isNotBlank() -> "編集中あり"
        section.savedContent.isNotBlank() -> "保存済みあり"
        else -> "未入力"
    }
    val includeMemo = sectionCount <= 5 || kotlin.math.abs(section.orderIndex - targetIndex) <= NearbySectionDistance
    val memo = if (includeMemo && content.isNotBlank()) {
        " / 内容メモ: ${content.limitPromptText(MaxOtherSectionMemoLength)}"
    } else {
        ""
    }
    return "${section.orderIndex + 1}. ${section.heading.normalizePromptText()} / 状態: $state$memo"
}

private fun String.normalizePromptText(): String =
    trim().replace(Regex("""\s+"""), " ")

private fun String.limitPromptText(maxLength: Int): String =
    if (length <= maxLength) this else take(maxLength)

internal fun buildProofreadingPrompt(request: ProofreadingRequest): String =
    """
    記事タイトル:
    ${request.articleTitle.ifBlank { "未設定" }}

    章節見出し:
    ${request.sectionHeading.ifBlank { "未設定" }}

    チェック対象本文:
    ${request.draftContent.ifBlank { request.savedContent }.ifBlank { "未入力" }}

    誤字脱字・表記ゆれ候補を返してください。結果は参考情報であり、断定しないでください。
    候補がない場合は MESSAGE だけ返してください。
    出力形式:
    MESSAGE: ユーザー向け要約
    ISSUE: 対象文字列
    SUGGEST: 修正候補
    REASON: 理由
    """.trimIndent()
