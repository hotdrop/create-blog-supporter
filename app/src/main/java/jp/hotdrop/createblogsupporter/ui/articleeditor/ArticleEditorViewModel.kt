package jp.hotdrop.createblogsupporter.ui.articleeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleResult
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private var hasLoadedTitle = false

    private val _uiState = MutableStateFlow(ArticleEditorUiState(isLoading = true))
    val uiState: StateFlow<ArticleEditorUiState> = _uiState.asStateFlow()

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

    fun onSectionClick() {
        _uiState.update { it.copy(message = ArticleEditorMessage.SectionEditNotImplemented) }
    }
}

data class ArticleEditorUiState(
    val articleId: Long? = null,
    val title: String = "",
    val sections: List<ArticleEditorSectionUiState> = emptyList(),
    val isLoading: Boolean = false,
    val isSavingTitle: Boolean = false,
    val titleError: Boolean = false,
    val message: ArticleEditorMessage? = null,
    val error: ArticleEditorError? = null,
) {
    val fullPreview: String
        get() = buildString {
            append("# ")
            append(title)
            sections.forEach { section ->
                append("\n\n## ")
                append(section.heading)
                if (section.content.isNotBlank()) {
                    append("\n\n")
                    append(section.content)
                }
            }
        }
}

data class ArticleEditorSectionUiState(
    val id: Long,
    val heading: String,
    val orderIndex: Int,
    val content: String,
    val draftContent: String,
    val userApproved: Boolean,
)

enum class ArticleEditorMessage {
    TitleSaved,
    TitleRequired,
    SaveFailed,
    SectionEditNotImplemented,
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
