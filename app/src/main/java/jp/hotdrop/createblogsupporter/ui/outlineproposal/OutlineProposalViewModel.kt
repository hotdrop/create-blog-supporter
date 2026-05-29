package jp.hotdrop.createblogsupporter.ui.outlineproposal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportResult
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposalRequest
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposalRequest
import jp.hotdrop.createblogsupporter.domain.usecase.AdoptOutlineProposalResult
import jp.hotdrop.createblogsupporter.domain.usecase.AdoptOutlineProposalUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateTitleProposalsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutlineProposalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
    private val generateTitleProposalsUseCase: GenerateTitleProposalsUseCase,
    private val generateOutlineProposalsUseCase: GenerateOutlineProposalsUseCase,
    private val adoptOutlineProposalUseCase: AdoptOutlineProposalUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private var hasGeneratedProposal = false

    private val _uiState = MutableStateFlow(OutlineProposalUiState(isLoading = true))
    val uiState: StateFlow<OutlineProposalUiState> = _uiState.asStateFlow()

    private val _adoptedEvent = MutableSharedFlow<Long>()
    val adoptedEvent: SharedFlow<Long> = _adoptedEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            observeArticleDraftUseCase(articleId).collect { article ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = OutlineProposalError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase1 && !hasGeneratedProposal -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = OutlineProposalError.NotPhase1,
                            )
                        }
                    }

                    !hasGeneratedProposal -> {
                        hasGeneratedProposal = true
                        updateGeneratedProposals(article)
                    }
                }
            }
        }
    }

    fun onTitleSelected(titleId: String) {
        _uiState.update { it.copy(selectedTitleId = titleId, message = null) }
    }

    fun onOutlineSelected(outlineId: String) {
        _uiState.update { it.copy(selectedOutlineId = outlineId, message = null) }
    }

    fun onAdoptClick() {
        val current = _uiState.value
        if (current.isAdopting) return
        val title = current.selectedTitle ?: return showInvalidProposal()
        val outline = current.selectedOutline ?: return showInvalidProposal()
        viewModelScope.launch {
            _uiState.update { it.copy(isAdopting = true, message = null) }
            when (
                adoptOutlineProposalUseCase(
                    articleId = articleId,
                    title = title.title,
                    headings = outline.headings,
                )
            ) {
                AdoptOutlineProposalResult.Adopted -> {
                    _uiState.update { it.copy(isAdopting = false) }
                    _adoptedEvent.emit(articleId)
                }

                AdoptOutlineProposalResult.InvalidProposal -> showInvalidProposal()
                AdoptOutlineProposalResult.NotPhase1OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isAdopting = false,
                            message = OutlineProposalMessage.AdoptFailed,
                        )
                    }
                }
            }
        }
    }

    private fun showInvalidProposal() {
        _uiState.update {
            it.copy(
                isAdopting = false,
                message = OutlineProposalMessage.SelectProposal,
            )
        }
    }

    private suspend fun updateGeneratedProposals(article: ArticleDraft) {
        val titleProposals = when (
            val result = generateTitleProposalsUseCase(
                TitleProposalRequest(
                    topic = article.topic,
                    detail = article.detail,
                ),
            )
        ) {
            is LlmSupportResult.Success -> result.value
            is LlmSupportResult.Failure -> return showGenerationFailed(result.reason)
        }
        val outlineProposals = when (
            val result = generateOutlineProposalsUseCase(
                OutlineProposalRequest(
                    topic = article.topic,
                    detail = article.detail,
                ),
            )
        ) {
            is LlmSupportResult.Success -> result.value
            is LlmSupportResult.Failure -> return showGenerationFailed(result.reason)
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                titleProposals = titleProposals,
                outlineProposals = outlineProposals,
                selectedTitleId = titleProposals.firstOrNull()?.id,
                selectedOutlineId = outlineProposals.firstOrNull()?.id,
                message = null,
                error = null,
            )
        }
    }

    private fun showGenerationFailed(reason: LlmSupportFailure) {
        _uiState.update {
            it.copy(
                isLoading = false,
                titleProposals = emptyList(),
                outlineProposals = emptyList(),
                selectedTitleId = null,
                selectedOutlineId = null,
                message = when (reason) {
                    LlmSupportFailure.ModelNotConfigured,
                    LlmSupportFailure.ModelFileMissing,
                    -> OutlineProposalMessage.ModelNotConfigured
                    LlmSupportFailure.InitializationFailed -> OutlineProposalMessage.ModelInitializationFailed
                    else -> OutlineProposalMessage.GenerationFailed
                },
            )
        }
    }
}

data class OutlineProposalUiState(
    val isLoading: Boolean = false,
    val isAdopting: Boolean = false,
    val titleProposals: List<TitleProposal> = emptyList(),
    val outlineProposals: List<OutlineProposal> = emptyList(),
    val selectedTitleId: String? = null,
    val selectedOutlineId: String? = null,
    val message: OutlineProposalMessage? = null,
    val error: OutlineProposalError? = null,
) {
    val selectedTitle: TitleProposal?
        get() = titleProposals.firstOrNull { it.id == selectedTitleId }

    val selectedOutline: OutlineProposal?
        get() = outlineProposals.firstOrNull { it.id == selectedOutlineId }
}

enum class OutlineProposalMessage {
    SelectProposal,
    AdoptFailed,
    ModelNotConfigured,
    ModelInitializationFailed,
    GenerationFailed,
}

enum class OutlineProposalError {
    NotFound,
    NotPhase1,
}
