package jp.hotdrop.createblogsupporter.ui.outlineproposal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.domain.usecase.AdoptOutlineProposalResult
import jp.hotdrop.createblogsupporter.domain.usecase.AdoptOutlineProposalUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateOutlineProposalStubUseCase
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
    private val generateOutlineProposalStubUseCase: GenerateOutlineProposalStubUseCase,
    private val adoptOutlineProposalUseCase: AdoptOutlineProposalUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private var hasGeneratedProposal = false

    private val _uiState = MutableStateFlow(OutlineProposalUiState(isLoading = true))
    val uiState: StateFlow<OutlineProposalUiState> = _uiState.asStateFlow()

    private val _adoptedEvent = MutableSharedFlow<Unit>()
    val adoptedEvent: SharedFlow<Unit> = _adoptedEvent.asSharedFlow()

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
                        val proposalSet = generateOutlineProposalStubUseCase(article)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                titleProposals = proposalSet.titleProposals,
                                outlineProposals = proposalSet.outlineProposals,
                                selectedTitleId = proposalSet.titleProposals.firstOrNull()?.id,
                                selectedOutlineId = proposalSet.outlineProposals.firstOrNull()?.id,
                                error = null,
                            )
                        }
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
                    _adoptedEvent.emit(Unit)
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
}

enum class OutlineProposalError {
    NotFound,
    NotPhase1,
}
