package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingCheckResult
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingIssue
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementSuggestion
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal

internal fun parseTitleProposals(text: String): List<TitleProposal>? {
    val titles = text.lineSequence()
        .map { it.trim() }
        .mapNotNull { line -> line.removePrefixOrNull("TITLE:")?.trim() }
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
                line.startsWith("OUTLINE:") -> {
                    flush()
                    currentName = line.removePrefix("OUTLINE:").trim()
                }
                line.startsWith("-") -> currentHeadings += line.removePrefix("-").trim()
            }
        }
    flush()

    return outlines.takeIf { it.size == 3 && it.all { outline -> outline.headings.isNotEmpty() } }
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
