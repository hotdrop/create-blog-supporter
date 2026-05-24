package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest

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
