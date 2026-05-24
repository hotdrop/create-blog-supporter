package jp.hotdrop.createblogsupporter.domain.model

sealed interface LlmSupportResult<out T> {
    data class Success<T>(val value: T) : LlmSupportResult<T>
    data class Failure(val reason: LlmSupportFailure) : LlmSupportResult<Nothing>
}

enum class LlmSupportFailure {
    Unavailable,
    ModelNotConfigured,
    ModelFileMissing,
    InitializationFailed,
    GenerationFailed,
    ResponseFormatInvalid,
}

data class TitleProposalRequest(
    val topic: String,
    val detail: String,
)

data class OutlineProposalRequest(
    val topic: String,
    val detail: String,
)

data class SectionSummaryRequest(
    val articleTitle: String,
    val sectionHeading: String,
    val savedContent: String,
    val draftContent: String,
)

data class SectionSummaryProposal(
    val summary: String,
)

data class SectionImprovementRequest(
    val articleTitle: String,
    val sectionHeading: String,
    val savedContent: String,
    val draftContent: String,
)

data class SectionImprovementSuggestion(
    val id: String,
    val title: String,
    val description: String,
)

data class ProofreadingRequest(
    val articleTitle: String,
    val sectionHeading: String,
    val savedContent: String,
    val draftContent: String,
)

data class ProofreadingCheckResult(
    val message: String,
    val issues: List<ProofreadingIssue>,
)

data class ProofreadingIssue(
    val id: String,
    val targetText: String,
    val suggestion: String,
    val reason: String,
)
