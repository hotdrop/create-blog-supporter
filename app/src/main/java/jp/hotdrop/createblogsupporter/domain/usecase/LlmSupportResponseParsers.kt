package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingCheckResult
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingIssue
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementSuggestion
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest

internal fun parseTitleProposals(text: String): List<TitleProposal>? {
    val titles = text.lineSequence()
        .map { it.trim() }
        .mapNotNull { line ->
            line.removePrefixOrNull("TITLE:")
                ?: line.removePrefixOrNull("TITLE：")
                ?: line.removePrefixOrNull("タイトル:")
                ?: line.removePrefixOrNull("タイトル：")
        }
        .filter { it.isNotBlank() }
        .take(3)
        .toList()
    return titles.takeIf { it.size == 3 }?.mapIndexed { index, title ->
        TitleProposal(id = "title-${index + 1}", title = title)
    }
}

internal fun parseOutlineProposals(text: String): List<OutlineProposal>? {
    val outlines = mutableListOf<OutlineProposal>()
    var currentName: String? = null
    var currentHeadings = mutableListOf<String>()

    fun flush() {
        val name = currentName?.trim().orEmpty()
        if (name.isNotBlank() && currentHeadings.isNotEmpty()) {
            outlines += OutlineProposal(
                id = "outline-${outlines.size + 1}",
                name = name,
                headings = currentHeadings.take(6),
            )
        }
        currentName = null
        currentHeadings = mutableListOf()
    }

    text.lineSequence()
        .map { it.trim() }
        .forEach { line ->
            when {
                line.startsWith("OUTLINE:") || line.startsWith("OUTLINE：") -> {
                    flush()
                    currentName = line
                        .removePrefix("OUTLINE:")
                        .removePrefix("OUTLINE：")
                        .trim()
                }
                line.startsWith("-") -> currentHeadings += line.removePrefix("-").trim()
            }
        }
    flush()

    return outlines.takeIf { it.size == 3 && it.all { outline -> outline.headings.isNotEmpty() } }
}

internal fun buildFallbackTitleProposals(
    request: TitleProposalRequest,
    generatedText: String,
): List<TitleProposal> {
    val generatedTitles = generatedText.lineSequence()
        .map { it.trim().cleanupGeneratedTitleLine() }
        .filter { it.isUsableProposalText() }
        .distinct()
        .take(3)
        .toList()

    val topic = request.topic.trim().ifBlank { "テックブログ" }
    val fallbackTitles = listOf(
        "$topic を実装から整理する",
        "$topic の設計判断を振り返る",
        "$topic で学んだことと次の改善",
    )

    return (generatedTitles + fallbackTitles)
        .filter { it.isNotBlank() }
        .distinct()
        .take(3)
        .mapIndexed { index, title ->
            TitleProposal(id = "title-${index + 1}", title = title)
        }
}

internal fun buildFallbackOutlineProposals(
    request: OutlineProposalRequest,
    generatedText: String,
): List<OutlineProposal> {
    val generatedHeadings = generatedText.lineSequence()
        .map { it.trim().cleanupGeneratedHeadingLine() }
        .filter { it.isUsableProposalText() }
        .filterNot { it.contains("OUTLINE", ignoreCase = true) || it.contains("構成名") }
        .distinct()
        .take(12)
        .toList()

    val topic = request.topic.trim().ifBlank { "テックブログ" }
    val detail = request.detail.trim()
    val fallbackHeadingGroups = listOf(
        listOf(
            "背景と書きたい理由",
            "前提と課題の整理",
            "$topic で試したこと",
            "学びと次の改善",
        ),
        listOf(
            "最初に困っていたこと",
            "選んだ設計方針",
            "実装で気づいたポイント",
            "運用して見直したいこと",
        ),
        listOf(
            "読者に伝えたい前提",
            detail.takeIf { it.isNotBlank() }?.let { "メモから見えた論点" } ?: "中心になる論点",
            "具体例で振り返る",
            "まとめと今後の課題",
        ),
    )

    val generatedGroups = generatedHeadings.chunked(4)
        .filter { it.size >= 3 }
        .map { headings -> headings.take(4) }

    val headingGroups = (generatedGroups + fallbackHeadingGroups)
        .take(3)

    return headingGroups.mapIndexed { index, headings ->
        OutlineProposal(
            id = "outline-${index + 1}",
            name = when (index) {
                0 -> "実装体験から整理する構成"
                1 -> "設計判断を中心にした構成"
                else -> "読者の疑問に答える構成"
            },
            headings = headings,
        )
    }
}

internal fun parseImprovementSuggestions(text: String): List<SectionImprovementSuggestion>? {
    val suggestions = mutableListOf<SectionImprovementSuggestion>()
    var title: String? = null
    var detail: String? = null

    fun flush() {
        val safeTitle = title?.trim().orEmpty()
        val safeDetail = detail?.trim().orEmpty()
        if (safeTitle.isNotBlank() && safeDetail.isNotBlank()) {
            suggestions += SectionImprovementSuggestion(
                id = "improvement-${suggestions.size + 1}",
                title = safeTitle,
                description = safeDetail,
            )
        }
        title = null
        detail = null
    }

    text.lineSequence()
        .map { it.trim() }
        .forEach { line ->
            when {
                line.startsWith("SUGGESTION:") -> {
                    flush()
                    title = line.removePrefix("SUGGESTION:").trim()
                }
                line.startsWith("DETAIL:") -> detail = line.removePrefix("DETAIL:").trim()
            }
        }
    flush()

    return suggestions.takeIf { it.isNotEmpty() }
}

internal fun parseProofreadingResult(text: String): ProofreadingCheckResult? {
    var message = ""
    val issues = mutableListOf<ProofreadingIssue>()
    var target: String? = null
    var suggestion: String? = null
    var reason: String? = null

    fun flush() {
        val safeTarget = target?.trim().orEmpty()
        val safeSuggestion = suggestion?.trim().orEmpty()
        val safeReason = reason?.trim().orEmpty()
        if (safeTarget.isNotBlank() && safeSuggestion.isNotBlank() && safeReason.isNotBlank()) {
            issues += ProofreadingIssue(
                id = "proofreading-${issues.size + 1}",
                targetText = safeTarget,
                suggestion = safeSuggestion,
                reason = safeReason,
            )
        }
        target = null
        suggestion = null
        reason = null
    }

    text.lineSequence()
        .map { it.trim() }
        .forEach { line ->
            when {
                line.startsWith("MESSAGE:") -> message = line.removePrefix("MESSAGE:").trim()
                line.startsWith("ISSUE:") -> {
                    flush()
                    target = line.removePrefix("ISSUE:").trim()
                }
                line.startsWith("SUGGEST:") -> suggestion = line.removePrefix("SUGGEST:").trim()
                line.startsWith("REASON:") -> reason = line.removePrefix("REASON:").trim()
            }
        }
    flush()

    return ProofreadingCheckResult(
        message = message.ifBlank { "誤字脱字・表記ゆれ候補を確認しました。最終判断は本文を読み直して行ってください。" },
        issues = issues,
    )
}

private fun String.removePrefixOrNull(prefix: String): String? =
    if (startsWith(prefix)) removePrefix(prefix) else null

private fun String.cleanupGeneratedTitleLine(): String =
    removePrefix("TITLE:")
        .removePrefix("TITLE：")
        .removePrefix("タイトル案:")
        .removePrefix("タイトル案：")
        .removePrefix("タイトル:")
        .removePrefix("タイトル：")
        .replace(Regex("""^\s*[-*]\s*"""), "")
        .replace(Regex("""^\s*\d+[.)、．]\s*"""), "")
        .trim()
        .trim('"', '「', '」')

private fun String.cleanupGeneratedHeadingLine(): String =
    removePrefix("見出し:")
        .removePrefix("見出し：")
        .replace(Regex("""^\s*[-*]\s*"""), "")
        .replace(Regex("""^\s*#{1,6}\s*"""), "")
        .replace(Regex("""^\s*\d+[.)、．]\s*"""), "")
        .trim()
        .trim('"', '「', '」')

private fun String.isUsableProposalText(): Boolean =
    isNotBlank() &&
        length <= 80 &&
        !startsWith("```") &&
        !contains("出力形式") &&
        !contains("題材:") &&
        !contains("題材：") &&
        !contains("詳細メモ")
