package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingCheckResult
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingIssue
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementSuggestion
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryProposal
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import javax.inject.Inject

class GenerateTitleProposalsUseCase @Inject constructor() {
    operator fun invoke(request: TitleProposalRequest): LlmSupportResult<List<TitleProposal>> {
        val topic = request.supportTopic()
        return LlmSupportResult.Success(
            listOf(
                TitleProposal(
                    id = "title-1",
                    title = "$topic を実装から理解する",
                ),
                TitleProposal(
                    id = "title-2",
                    title = "$topic の設計判断を振り返る",
                ),
                TitleProposal(
                    id = "title-3",
                    title = "$topic でつまずいた点と改善策",
                ),
            ),
        )
    }
}

class GenerateOutlineProposalsUseCase @Inject constructor() {
    operator fun invoke(request: OutlineProposalRequest): LlmSupportResult<List<OutlineProposal>> =
        LlmSupportResult.Success(
            listOf(
                OutlineProposal(
                    id = "outline-1",
                    name = "実装手順から整理する構成",
                    headings = listOf(
                        "背景と解決したかったこと",
                        "最初に決めた設計方針",
                        "実装で詰まったポイント",
                        "次に同じことをするなら",
                    ),
                ),
                OutlineProposal(
                    id = "outline-2",
                    name = "読者の疑問に答える構成",
                    headings = listOf(
                        "なぜこのテーマを扱うのか",
                        "前提知識と全体像",
                        "具体例で見る実装の流れ",
                        "運用して分かった注意点",
                    ),
                ),
                OutlineProposal(
                    id = "outline-3",
                    name = "失敗と改善を中心にした構成",
                    headings = listOf(
                        "当初の課題",
                        "うまくいかなかった実装",
                        "改善した設計",
                        "得られた学び",
                    ),
                ),
            ),
        )
}

class GenerateSectionSummaryUseCase @Inject constructor() {
    operator fun invoke(request: SectionSummaryRequest): LlmSupportResult<SectionSummaryProposal> {
        val heading = request.sectionHeading.ifBlank { "この章節" }
        val draftHint = request.draftContent.takeIf { it.isNotBlank() }
            ?: request.savedContent.takeIf { it.isNotBlank() }
        val summary = if (draftHint == null) {
            "$heading で扱う論点を、背景・判断・実装メモに分けて整理する案です。"
        } else {
            "$heading の既存メモをもとに、読者に伝えたい判断と具体例を整理する案です。"
        }
        return LlmSupportResult.Success(SectionSummaryProposal(summary = summary))
    }
}

class GenerateSectionImprovementSuggestionsUseCase @Inject constructor() {
    operator fun invoke(
        request: SectionImprovementRequest,
    ): LlmSupportResult<List<SectionImprovementSuggestion>> =
        LlmSupportResult.Success(
            listOf(
                SectionImprovementSuggestion(
                    id = "improvement-1",
                    title = "背景を先に置く",
                    description = "読者が前提を追いやすいように、実装前の課題を冒頭で短く説明する案です。",
                ),
                SectionImprovementSuggestion(
                    id = "improvement-2",
                    title = "判断理由を明示する",
                    description = "採用した設計だけでなく、選ばなかった選択肢と理由も一文で補う案です。",
                ),
                SectionImprovementSuggestion(
                    id = "improvement-3",
                    title = "具体例を追加する",
                    description = "抽象的な説明が続く箇所に、実際の画面やコード上の例を差し込む案です。",
                ),
            ),
        )
}

class CheckSectionProofreadingUseCase @Inject constructor() {
    operator fun invoke(request: ProofreadingRequest): LlmSupportResult<ProofreadingCheckResult> {
        val text = request.draftContent.ifBlank { request.savedContent }
        val issues = buildList {
            if ("LiteRM" in text || "LiteRT" in text) {
                add(
                    ProofreadingIssue(
                        id = "proofreading-1",
                        targetText = if ("LiteRM" in text) "LiteRM" else "LiteRT",
                        suggestion = "LiteRT-LM",
                        reason = "SDK名の表記をプロジェクト内の正式表記にそろえる候補です。",
                    ),
                )
            }
            if ("Simpel" in text) {
                add(
                    ProofreadingIssue(
                        id = "proofreading-2",
                        targetText = "Simpel",
                        suggestion = "Simple",
                        reason = "英単語の綴りを確認する候補です。",
                    ),
                )
            }
        }
        val message = if (issues.isEmpty()) {
            "明確な誤字脱字候補は見つかりませんでした。最終判断は本文を読み直して行ってください。"
        } else {
            "誤字脱字・表記ゆれの候補が ${issues.size} 件あります。必要なものだけ反映してください。"
        }
        return LlmSupportResult.Success(
            ProofreadingCheckResult(
                message = message,
                issues = issues,
            ),
        )
    }
}

private fun TitleProposalRequest.supportTopic(): String =
    topic.trim().ifBlank { "テックブログ" }
