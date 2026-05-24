package jp.hotdrop.createblogsupporter.ui.sectioneditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.usecase.ArticleSectionContentOperationResult
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ResetArticleSectionDraftToSavedUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.SaveArticleSectionContentUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdateArticleSectionDraftContentUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdateArticleSectionUserApprovedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DraftAutoSaveDelayMillis = 500L

@HiltViewModel
class SectionEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
    observeArticleSectionUseCase: ObserveArticleSectionUseCase,
    private val updateArticleSectionDraftContentUseCase: UpdateArticleSectionDraftContentUseCase,
    private val saveArticleSectionContentUseCase: SaveArticleSectionContentUseCase,
    private val resetArticleSectionDraftToSavedUseCase: ResetArticleSectionDraftToSavedUseCase,
    private val updateArticleSectionUserApprovedUseCase: UpdateArticleSectionUserApprovedUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private val sectionId: Long = checkNotNull(savedStateHandle["sectionId"])
    private var hasLocalDraftEdit = false
    private var draftAutoSaveJob: Job? = null

    private val _uiState = MutableStateFlow(SectionEditorUiState(isLoading = true))
    val uiState: StateFlow<SectionEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeArticleDraftUseCase(articleId),
                observeArticleSectionUseCase(articleId, sectionId),
            ) { article, section ->
                article to section
            }.collect { (article, section) ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionEditorError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionEditorError.NotPhase2,
                            )
                        }
                    }

                    section == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionEditorError.NotFound,
                            )
                        }
                    }

                    else -> applySection(section)
                }
            }
        }
    }

    fun onDraftContentChanged(value: String) {
        hasLocalDraftEdit = true
        _uiState.update {
            it.copy(
                draftContent = value,
                message = null,
            )
        }
        draftAutoSaveJob?.cancel()
        draftAutoSaveJob = viewModelScope.launch {
            delay(DraftAutoSaveDelayMillis)
            autoSaveDraft(value)
        }
    }

    fun onSaveContentClick() {
        val current = _uiState.value
        if (current.isSavingContent) return
        draftAutoSaveJob?.cancel()
        draftAutoSaveJob = null
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingContent = true,
                    isAutoSavingDraft = false,
                    message = null,
                )
            }
            try {
                val draftResult = updateArticleSectionDraftContentUseCase(articleId, sectionId, current.draftContent)
                if (draftResult != ArticleSectionContentOperationResult.Updated) {
                    showOperationFailed(isSavingContent = true)
                    return@launch
                }
                when (saveArticleSectionContentUseCase(articleId, sectionId)) {
                    ArticleSectionContentOperationResult.Updated -> {
                        hasLocalDraftEdit = false
                        _uiState.update {
                            it.copy(
                                isSavingContent = false,
                                content = current.draftContent,
                                userApproved = false,
                                message = SectionEditorMessage.ContentSaved,
                            )
                        }
                    }

                    ArticleSectionContentOperationResult.NotPhase2OrMissing -> showOperationFailed(isSavingContent = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showOperationFailed(isSavingContent = true)
            }
        }
    }

    fun onResetDraftClick() {
        val current = _uiState.value
        if (current.isResettingDraft) return
        draftAutoSaveJob?.cancel()
        draftAutoSaveJob = null
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResettingDraft = true,
                    isAutoSavingDraft = false,
                    message = null,
                )
            }
            try {
                when (resetArticleSectionDraftToSavedUseCase(articleId, sectionId)) {
                    ArticleSectionContentOperationResult.Updated -> {
                        hasLocalDraftEdit = false
                        _uiState.update {
                            it.copy(
                                isResettingDraft = false,
                                draftContent = current.content,
                                message = SectionEditorMessage.DraftReset,
                            )
                        }
                    }

                    ArticleSectionContentOperationResult.NotPhase2OrMissing -> showOperationFailed(isResettingDraft = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showOperationFailed(isResettingDraft = true)
            }
        }
    }

    fun onUserApprovedChanged(value: Boolean) {
        val current = _uiState.value
        if (current.isUpdatingApproval || current.userApproved == value) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingApproval = true,
                    userApproved = value,
                    message = null,
                )
            }
            try {
                when (updateArticleSectionUserApprovedUseCase(articleId, sectionId, value)) {
                    ArticleSectionContentOperationResult.Updated -> {
                        _uiState.update {
                            it.copy(
                                isUpdatingApproval = false,
                                message = if (value) {
                                    SectionEditorMessage.MarkedApproved
                                } else {
                                    SectionEditorMessage.MarkedUnapproved
                                },
                            )
                        }
                    }

                    ArticleSectionContentOperationResult.NotPhase2OrMissing -> {
                        _uiState.update {
                            it.copy(
                                isUpdatingApproval = false,
                                userApproved = current.userApproved,
                                message = SectionEditorMessage.OperationFailed,
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingApproval = false,
                        userApproved = current.userApproved,
                        message = SectionEditorMessage.OperationFailed,
                    )
                }
            }
        }
    }

    fun onToggleComparisonClick() {
        _uiState.update { it.copy(showComparison = !it.showComparison) }
    }

    private fun applySection(section: ArticleSection) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                heading = section.heading,
                orderIndex = section.orderIndex,
                content = section.content,
                draftContent = if (hasLocalDraftEdit) it.draftContent else section.draftContent,
                userApproved = section.userApproved,
            )
        }
    }

    private suspend fun autoSaveDraft(draftContent: String) {
        _uiState.update { it.copy(isAutoSavingDraft = true) }
        try {
            when (updateArticleSectionDraftContentUseCase(articleId, sectionId, draftContent)) {
                ArticleSectionContentOperationResult.Updated -> {
                    hasLocalDraftEdit = false
                    _uiState.update {
                        it.copy(
                            isAutoSavingDraft = false,
                            message = SectionEditorMessage.DraftAutoSaved,
                        )
                    }
                }

                ArticleSectionContentOperationResult.NotPhase2OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isAutoSavingDraft = false,
                            message = SectionEditorMessage.OperationFailed,
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update {
                it.copy(
                    isAutoSavingDraft = false,
                    message = SectionEditorMessage.OperationFailed,
                )
            }
        }
    }

    private fun showOperationFailed(
        isSavingContent: Boolean = false,
        isResettingDraft: Boolean = false,
    ) {
        _uiState.update {
            it.copy(
                isSavingContent = if (isSavingContent) false else it.isSavingContent,
                isResettingDraft = if (isResettingDraft) false else it.isResettingDraft,
                message = SectionEditorMessage.OperationFailed,
            )
        }
    }
}

data class SectionEditorUiState(
    val heading: String = "",
    val orderIndex: Int = 0,
    val content: String = "",
    val draftContent: String = "",
    val userApproved: Boolean = false,
    val isLoading: Boolean = false,
    val isSavingContent: Boolean = false,
    val isResettingDraft: Boolean = false,
    val isAutoSavingDraft: Boolean = false,
    val isUpdatingApproval: Boolean = false,
    val showComparison: Boolean = false,
    val message: SectionEditorMessage? = null,
    val error: SectionEditorError? = null,
) {
    val hasUnsavedChanges: Boolean
        get() = content != draftContent

    val comparisonRows: List<SectionComparisonRow>
        get() = buildComparisonRows(content, draftContent)
}

data class SectionComparisonRow(
    val savedText: String,
    val draftText: String,
    val type: SectionComparisonType,
)

enum class SectionComparisonType {
    Unchanged,
    Added,
    Deleted,
    Changed,
}

enum class SectionEditorMessage {
    DraftAutoSaved,
    ContentSaved,
    DraftReset,
    MarkedApproved,
    MarkedUnapproved,
    OperationFailed,
}

enum class SectionEditorError {
    NotFound,
    NotPhase2,
}

private fun buildComparisonRows(
    savedContent: String,
    draftContent: String,
): List<SectionComparisonRow> {
    val savedLines = savedContent.linesForComparison()
    val draftLines = draftContent.linesForComparison()
    val maxSize = maxOf(savedLines.size, draftLines.size)
    if (maxSize == 0) {
        return listOf(SectionComparisonRow("", "", SectionComparisonType.Unchanged))
    }
    return (0 until maxSize).map { index ->
        val saved = savedLines.getOrNull(index).orEmpty()
        val draft = draftLines.getOrNull(index).orEmpty()
        val type = when {
            saved == draft -> SectionComparisonType.Unchanged
            saved.isEmpty() -> SectionComparisonType.Added
            draft.isEmpty() -> SectionComparisonType.Deleted
            else -> SectionComparisonType.Changed
        }
        SectionComparisonRow(
            savedText = saved,
            draftText = draft,
            type = type,
        )
    }
}

private fun String.linesForComparison(): List<String> =
    lineSequence()
        .map { it.trimEnd() }
        .toList()
        .dropLastWhile { it.isEmpty() }
