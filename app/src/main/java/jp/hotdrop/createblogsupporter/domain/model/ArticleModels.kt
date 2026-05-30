package jp.hotdrop.createblogsupporter.domain.model

enum class ArticlePhase(val storageValue: String) {
    Phase1("phase1"),
    Phase2("phase2"),
}

enum class ArticleStatus(val storageValue: String) {
    Draft("draft"),
    ReadyToExport("readyToExport"),
    Exported("exported"),
}

enum class ProofreadStatus(val storageValue: String) {
    Unchecked("unchecked"),
    Checking("checking"),
    Checked("checked"),
    Error("error"),
}

enum class ArticleSectionMoveDirection {
    Up,
    Down,
}

data class ArticleDraft(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
    val topic: String,
    val detail: String,
    val status: ArticleStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val exportedAt: Long?,
)

data class ArticleDraftSummary(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
    val topic: String,
    val status: ArticleStatus,
    val updatedAt: Long,
    val allSectionsApproved: Boolean,
)

data class ArticleDraftHeader(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
)

data class ArticleSection(
    val id: Long,
    val articleId: Long,
    val heading: String,
    val orderIndex: Int,
    val content: String,
    val draftContent: String,
    val proofreadStatus: ProofreadStatus,
    val proofreadMessage: String?,
    val userApproved: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSavedAt: Long?,
    val draftUpdatedAt: Long?,
)

data class TitleProposal(
    val id: String,
    val title: String,
)

data class OutlineProposal(
    val id: String,
    val name: String,
    val headings: List<String>,
)
