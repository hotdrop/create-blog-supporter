package jp.hotdrop.createblogsupporter.ui.articletitleedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftHeaderUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleResult
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleUseCase
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
class ArticleTitleEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftHeaderUseCase: ObserveArticleDraftHeaderUseCase,
    private val updatePhase2TitleUseCase: UpdatePhase2TitleUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private var hasLoadedTitle = false

    private val _uiState = MutableStateFlow(ArticleTitleEditUiState(isLoading = true))
    val uiState: StateFlow<ArticleTitleEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ArticleTitleEditEvent>()
    val events: SharedFlow<ArticleTitleEditEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            observeArticleDraftHeaderUseCase(articleId).collect { article ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticleTitleEditError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticleTitleEditError.NotPhase2,
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = if (hasLoadedTitle) it.title else article.title,
                                error = null,
                            )
                        }
                        hasLoadedTitle = true
                    }
                }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it.copy(
                title = value,
                titleError = false,
                message = null,
            )
        }
    }

    fun onSaveClick() {
        val current = _uiState.value
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    titleError = false,
                    message = null,
                )
            }
            when (updatePhase2TitleUseCase(articleId, current.title)) {
                UpdatePhase2TitleResult.Updated -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            title = current.title.trim(),
                        )
                    }
                    _events.emit(ArticleTitleEditEvent.Saved)
                }

                UpdatePhase2TitleResult.InvalidTitle -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            titleError = true,
                            message = ArticleTitleEditMessage.TitleRequired,
                        )
                    }
                }

                UpdatePhase2TitleResult.NotPhase2OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = ArticleTitleEditMessage.SaveFailed,
                        )
                    }
                }
            }
        }
    }
}

data class ArticleTitleEditUiState(
    val title: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: Boolean = false,
    val message: ArticleTitleEditMessage? = null,
    val error: ArticleTitleEditError? = null,
)

sealed interface ArticleTitleEditMessage {
    data object TitleRequired : ArticleTitleEditMessage
    data object SaveFailed : ArticleTitleEditMessage
}

sealed interface ArticleTitleEditEvent {
    data object Saved : ArticleTitleEditEvent
}

enum class ArticleTitleEditError {
    NotFound,
    NotPhase2,
}
