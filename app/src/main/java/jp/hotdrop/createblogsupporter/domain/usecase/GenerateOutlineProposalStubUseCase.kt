package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalSet
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import javax.inject.Inject

class GenerateOutlineProposalStubUseCase @Inject constructor() {
    operator fun invoke(article: ArticleDraft): OutlineProposalSet {
        val topic = article.topic.ifBlank { "テックブログ" }
        return OutlineProposalSet(
            titleProposals = listOf(
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
            outlineProposals = listOf(
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
}
