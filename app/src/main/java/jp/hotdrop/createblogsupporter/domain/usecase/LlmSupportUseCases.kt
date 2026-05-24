package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingCheckResult
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionImprovementSuggestion
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryProposal
import jp.hotdrop.createblogsupporter.domain.model.SectionSummaryRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

class GenerateTitleProposalsUseCase @Inject constructor(
    private val llmClient: BlogSupportLlmClient,
) {
    suspend operator fun invoke(request: TitleProposalRequest): LlmSupportResult<List<TitleProposal>> =
        generateAndParse(
            llmClient = llmClient,
            prompt = buildTitlePrompt(request),
            parser = ::parseTitleProposals,
        )
}

class GenerateOutlineProposalsUseCase @Inject constructor(
    private val llmClient: BlogSupportLlmClient,
) {
    suspend operator fun invoke(request: OutlineProposalRequest): LlmSupportResult<List<OutlineProposal>> =
        generateAndParse(
            llmClient = llmClient,
            prompt = buildOutlinePrompt(request),
            parser = ::parseOutlineProposals,
        )
}

class GenerateSectionSummaryUseCase @Inject constructor(
    private val llmClient: BlogSupportLlmClient,
) {
    suspend operator fun invoke(request: SectionSummaryRequest): LlmSupportResult<SectionSummaryProposal> =
        generateAndParse(
            llmClient = llmClient,
            prompt = buildSectionSummaryPrompt(request),
            parser = { text -> SectionSummaryProposal(summary = text.trim()).takeIf { it.summary.isNotBlank() } },
        )
}

class GenerateSectionImprovementSuggestionsUseCase @Inject constructor(
    private val llmClient: BlogSupportLlmClient,
) {
    suspend operator fun invoke(
        request: SectionImprovementRequest,
    ): LlmSupportResult<List<SectionImprovementSuggestion>> =
        generateAndParse(
            llmClient = llmClient,
            prompt = buildSectionImprovementPrompt(request),
            parser = ::parseImprovementSuggestions,
        )
}

class CheckSectionProofreadingUseCase @Inject constructor(
    private val llmClient: BlogSupportLlmClient,
) {
    suspend operator fun invoke(request: ProofreadingRequest): LlmSupportResult<ProofreadingCheckResult> =
        generateAndParse(
            llmClient = llmClient,
            prompt = buildProofreadingPrompt(request),
            parser = ::parseProofreadingResult,
        )
}

private suspend fun <T> generateAndParse(
    llmClient: BlogSupportLlmClient,
    prompt: String,
    parser: (String) -> T?,
    temperature: Float = 0.4f,
): LlmSupportResult<T> =
    try {
        var latestText = ""
        llmClient.streamText(BlogSupportLlmRequest(prompt = prompt, temperature = temperature)).collect { text ->
            latestText = text
        }
        val value = parser(latestText)
        if (value == null) {
            LlmSupportResult.Failure(LlmSupportFailure.ResponseFormatInvalid)
        } else {
            LlmSupportResult.Success(value)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: LlmSupportException) {
        LlmSupportResult.Failure(e.failure)
    } catch (_: Exception) {
        LlmSupportResult.Failure(LlmSupportFailure.GenerationFailed)
    }
