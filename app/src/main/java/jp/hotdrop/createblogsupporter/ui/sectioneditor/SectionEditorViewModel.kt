package jp.hotdrop.createblogsupporter.ui.sectioneditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationSectionContext
import jp.hotdrop.createblogsupporter.domain.model.countEditableContentCharacters
import jp.hotdrop.createblogsupporter.domain.usecase.ArticleSectionContentOperationResult
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionPastePromptUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ResetArticleSectionDraftToSavedUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.SaveArticleSectionContentUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdateArticleSectionDraftContentUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdateArticleSectionUserApprovedUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    observeArticleSectionsUseCase: ObserveArticleSectionsUseCase,
    private val updateArticleSectionDraftContentUseCase: UpdateArticleSectionDraftContentUseCase,
    private val saveArticleSectionContentUseCase: SaveArticleSectionContentUseCase,
    private val resetArticleSectionDraftToSavedUseCase: ResetArticleSectionDraftToSavedUseCase,
    private val updateArticleSectionUserApprovedUseCase: UpdateArticleSectionUserApprovedUseCase,
    private val generateSectionPastePromptUseCase: GenerateSectionPastePromptUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private val sectionId: Long = checkNotNull(savedStateHandle["sectionId"])
    private var hasLocalDraftEdit = false
    private var draftAutoSaveJob: Job? = null

    private val _uiState = MutableStateFlow(SectionEditorUiState(isLoading = true))
    val uiState: StateFlow<SectionEditorUiState> = _uiState.asStateFlow()

    private val _copyEvents = MutableSharedFlow<String>()
    val copyEvents: SharedFlow<String> = _copyEvents.asSharedFlow()

    private val _closeEvents = MutableSharedFlow<Unit>()
    val closeEvents: SharedFlow<Unit> = _closeEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                observeArticleDraftUseCase(articleId),
                observeArticleSectionsUseCase(articleId),
            ) { article, sections ->
                article to sections
            }.collect { (article, sections) ->
                val section = sections.firstOrNull { it.id == sectionId }
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

                    else -> applyArticleAndSection(article, section, sections)
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

    fun onDiscardChangesClick() {
        val current = _uiState.value
        if (current.isDiscardingChanges) return
        draftAutoSaveJob?.cancel()
        draftAutoSaveJob = null
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDiscardingChanges = true,
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
                                isDiscardingChanges = false,
                                draftContent = current.content,
                                message = null,
                            )
                        }
                        _closeEvents.emit(Unit)
                    }

                    ArticleSectionContentOperationResult.NotPhase2OrMissing -> showOperationFailed(isDiscardingChanges = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                showOperationFailed(isDiscardingChanges = true)
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

    fun onConsultationInputChanged(value: String) {
        _uiState.update {
            it.copy(
                consultationInput = value,
                consultationMessage = null,
            )
        }
    }

    fun onCreatePastePromptClick() {
        val current = _uiState.value
        val targetSection = SectionConsultationSectionContext(
            orderIndex = current.orderIndex,
            heading = current.heading,
            savedContent = current.content,
            draftContent = current.draftContent,
            isTarget = true,
        )
        val outlineContext = current.outlineContext
            .filterNot { it.orderIndex == current.orderIndex || it.isTarget }
            .plus(targetSection)
            .sortedBy { it.orderIndex }
        val pastePrompt = generateSectionPastePromptUseCase(
            SectionConsultationRequest(
                articleTitle = current.articleTitle,
                topic = current.topic,
                detail = current.detail,
                targetSection = targetSection,
                outlineContext = outlineContext,
                userQuestion = current.consultationInput.trim(),
            ),
        )
        _uiState.update {
            it.copy(
                consultationAnswer = pastePrompt,
                consultationMessage = null,
            )
        }
    }

    fun onCopyConsultationAnswerClick() {
        val answer = _uiState.value.consultationAnswer
        if (answer.isBlank()) return
        viewModelScope.launch {
            _copyEvents.emit(answer)
            _uiState.update { it.copy(consultationMessage = SectionEditorConsultationMessage.Copied) }
        }
    }

    private fun applyArticleAndSection(
        article: ArticleDraft,
        section: ArticleSection,
        sections: List<ArticleSection>,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                articleTitle = article.title,
                topic = article.topic,
                detail = article.detail,
                heading = section.heading,
                orderIndex = section.orderIndex,
                content = section.content,
                draftContent = if (hasLocalDraftEdit) it.draftContent else section.draftContent,
                userApproved = section.userApproved,
                outlineContext = sections.map { sectionContext ->
                    sectionContext.toConsultationContext(isTarget = sectionContext.id == sectionId)
                },
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
        isDiscardingChanges: Boolean = false,
    ) {
        _uiState.update {
            it.copy(
                isSavingContent = if (isSavingContent) false else it.isSavingContent,
                isDiscardingChanges = if (isDiscardingChanges) false else it.isDiscardingChanges,
                message = SectionEditorMessage.OperationFailed,
            )
        }
    }

}

data class SectionEditorUiState(
    val articleTitle: String = "",
    val topic: String = "",
    val detail: String = "",
    val heading: String = "",
    val orderIndex: Int = 0,
    val content: String = "",
    val draftContent: String = "",
    val outlineContext: List<SectionConsultationSectionContext> = emptyList(),
    val userApproved: Boolean = false,
    val isLoading: Boolean = false,
    val isSavingContent: Boolean = false,
    val isDiscardingChanges: Boolean = false,
    val isAutoSavingDraft: Boolean = false,
    val isUpdatingApproval: Boolean = false,
    val consultationInput: String = "",
    val consultationAnswer: String = "",
    val consultationMessage: SectionEditorConsultationMessage? = null,
    val message: SectionEditorMessage? = null,
    val error: SectionEditorError? = null,
) {
    val hasUnsavedChanges: Boolean
        get() = content != draftContent

    val currentCharacterCount: Int
        get() = countEditableContentCharacters(
            content = content,
            draftContent = draftContent,
        )
}

enum class SectionEditorMessage {
    DraftAutoSaved,
    ContentSaved,
    MarkedApproved,
    MarkedUnapproved,
    OperationFailed,
}

enum class SectionEditorConsultationMessage {
    Copied,
}

enum class SectionEditorError {
    NotFound,
    NotPhase2,
}

private fun ArticleSection.toConsultationContext(isTarget: Boolean): SectionConsultationSectionContext =
    SectionConsultationSectionContext(
        orderIndex = orderIndex,
        heading = heading,
        savedContent = content,
        draftContent = draftContent,
        isTarget = isTarget,
    )
