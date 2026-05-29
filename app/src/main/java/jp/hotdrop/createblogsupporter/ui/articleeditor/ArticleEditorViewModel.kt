package jp.hotdrop.createblogsupporter.ui.articleeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ExportMarkdownResult
import jp.hotdrop.createblogsupporter.domain.usecase.ExportMarkdownUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleResult
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleUseCase
import kotlinx.coroutines.CancellationException
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
class ArticleEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
    observeArticleSectionsUseCase: ObserveArticleSectionsUseCase,
    private val updatePhase2TitleUseCase: UpdatePhase2TitleUseCase,
    private val exportMarkdownUseCase: ExportMarkdownUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private var hasLoadedTitle = false

    private val _uiState = MutableStateFlow(ArticleEditorUiState(isLoading = true))
    val uiState: StateFlow<ArticleEditorUiState> = _uiState.asStateFlow()

    private val _shareEvents = MutableSharedFlow<ArticleEditorShareEvent>()
    val shareEvents: SharedFlow<ArticleEditorShareEvent> = _shareEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            observeArticleDraftUseCase(articleId).collect { article ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticleEditorError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticleEditorError.NotPhase2,
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                articleId = article.id,
                                title = if (hasLoadedTitle) it.title else article.title,
                                error = null,
                            )
                        }
                        hasLoadedTitle = true
                    }
                }
            }
        }
        viewModelScope.launch {
            observeArticleSectionsUseCase(articleId).collect { sections ->
                _uiState.update {
                    it.copy(sections = sections.map { section -> section.toUiState() })
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

    fun onSaveTitleClick() {
        val current = _uiState.value
        if (current.isSavingTitle) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingTitle = true,
                    titleError = false,
                    message = null,
                )
            }
            when (updatePhase2TitleUseCase(articleId, current.title)) {
                UpdatePhase2TitleResult.Updated -> {
                    _uiState.update {
                        it.copy(
                            isSavingTitle = false,
                            title = current.title.trim(),
                            message = ArticleEditorMessage.TitleSaved,
                        )
                    }
                }

                UpdatePhase2TitleResult.InvalidTitle -> {
                    _uiState.update {
                        it.copy(
                            isSavingTitle = false,
                            titleError = true,
                            message = ArticleEditorMessage.TitleRequired,
                        )
                    }
                }

                UpdatePhase2TitleResult.NotPhase2OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isSavingTitle = false,
                            message = ArticleEditorMessage.SaveFailed,
                        )
                    }
                }
            }
        }
    }

    fun onExportMarkdownClick() {
        val current = _uiState.value
        if (current.isExportingMarkdown) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExportingMarkdown = true,
                    message = null,
                )
            }
            try {
                when (val result = exportMarkdownUseCase(articleId)) {
                    is ExportMarkdownResult.Exported -> {
                        _uiState.update {
                            it.copy(
                                isExportingMarkdown = false,
                                message = ArticleEditorMessage.MarkdownExported,
                            )
                        }
                        _shareEvents.emit(
                            ArticleEditorShareEvent.ShareMarkdown(
                                uriString = result.file.uriString,
                                fileName = result.file.fileName,
                                title = result.title,
                            ),
                        )
                    }

                    ExportMarkdownResult.BlankTitle -> {
                        _uiState.update {
                            it.copy(
                                isExportingMarkdown = false,
                                message = ArticleEditorMessage.ExportTitleRequired,
                            )
                        }
                    }

                    ExportMarkdownResult.NotPhase2OrMissing -> {
                        _uiState.update {
                            it.copy(
                                isExportingMarkdown = false,
                                message = ArticleEditorMessage.ExportNotPhase2OrMissing,
                            )
                        }
                    }

                    is ExportMarkdownResult.UnapprovedSections -> {
                        _uiState.update {
                            it.copy(
                                isExportingMarkdown = false,
                                message = ArticleEditorMessage.ExportUnapprovedSections(result.count),
                            )
                        }
                    }

                    ExportMarkdownResult.WriteFailed -> {
                        _uiState.update {
                            it.copy(
                                isExportingMarkdown = false,
                                message = ArticleEditorMessage.ExportWriteFailed,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isExportingMarkdown = false,
                        message = ArticleEditorMessage.ExportWriteFailed,
                    )
                }
            }
        }
    }
}

data class ArticleEditorUiState(
    val articleId: Long? = null,
    val title: String = "",
    val sections: List<ArticleEditorSectionUiState> = emptyList(),
    val isLoading: Boolean = false,
    val isSavingTitle: Boolean = false,
    val isExportingMarkdown: Boolean = false,
    val titleError: Boolean = false,
    val message: ArticleEditorMessage? = null,
    val error: ArticleEditorError? = null,
) {
}

data class ArticleEditorSectionUiState(
    val id: Long,
    val heading: String,
    val orderIndex: Int,
    val content: String,
    val draftContent: String,
    val userApproved: Boolean,
)

sealed interface ArticleEditorMessage {
    data object TitleSaved : ArticleEditorMessage
    data object TitleRequired : ArticleEditorMessage
    data object SaveFailed : ArticleEditorMessage
    data object MarkdownExported : ArticleEditorMessage
    data object ExportTitleRequired : ArticleEditorMessage
    data object ExportNotPhase2OrMissing : ArticleEditorMessage
    data class ExportUnapprovedSections(val count: Int) : ArticleEditorMessage
    data object ExportWriteFailed : ArticleEditorMessage
}

sealed interface ArticleEditorShareEvent {
    data class ShareMarkdown(
        val uriString: String,
        val fileName: String,
        val title: String,
    ) : ArticleEditorShareEvent
}

enum class ArticleEditorError {
    NotFound,
    NotPhase2,
}

private fun ArticleSection.toUiState(): ArticleEditorSectionUiState =
    ArticleEditorSectionUiState(
        id = id,
        heading = heading,
        orderIndex = orderIndex,
        content = content,
        draftContent = draftContent,
        userApproved = userApproved,
    )
