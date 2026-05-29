package jp.hotdrop.createblogsupporter.ui.outlineedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import jp.hotdrop.createblogsupporter.domain.usecase.AddArticleSectionUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ArticleSectionOperationResult
import jp.hotdrop.createblogsupporter.domain.usecase.DeleteArticleSectionResult
import jp.hotdrop.createblogsupporter.domain.usecase.DeleteArticleSectionUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.MoveArticleSectionUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftHeaderUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdateArticleSectionHeadingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OutlineEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftHeaderUseCase: ObserveArticleDraftHeaderUseCase,
    observeArticleSectionsUseCase: ObserveArticleSectionsUseCase,
    private val addArticleSectionUseCase: AddArticleSectionUseCase,
    private val updateArticleSectionHeadingUseCase: UpdateArticleSectionHeadingUseCase,
    private val deleteArticleSectionUseCase: DeleteArticleSectionUseCase,
    private val moveArticleSectionUseCase: MoveArticleSectionUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])

    private val _uiState = MutableStateFlow(OutlineEditUiState(isLoading = true))
    val uiState: StateFlow<OutlineEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeArticleDraftHeaderUseCase(articleId).collect { article ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = OutlineEditError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = OutlineEditError.NotPhase2,
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = article.title,
                                error = null,
                            )
                        }
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

    fun onAddClick() {
        _uiState.update {
            it.copy(
                dialog = OutlineEditDialog.Add(heading = ""),
                message = null,
            )
        }
    }

    fun onEditHeadingClick(section: OutlineEditSectionUiState) {
        _uiState.update {
            it.copy(
                dialog = OutlineEditDialog.EditHeading(
                    sectionId = section.id,
                    heading = section.heading,
                ),
                message = null,
            )
        }
    }

    fun onDeleteClick(section: OutlineEditSectionUiState) {
        _uiState.update {
            it.copy(
                dialog = OutlineEditDialog.ConfirmDelete(
                    sectionId = section.id,
                    heading = section.heading,
                ),
                message = null,
            )
        }
    }

    fun onDialogHeadingChanged(value: String) {
        _uiState.update { current ->
            val updatedDialog = when (val dialog = current.dialog) {
                is OutlineEditDialog.Add -> dialog.copy(heading = value, headingError = false)
                is OutlineEditDialog.EditHeading -> dialog.copy(heading = value, headingError = false)
                is OutlineEditDialog.ConfirmDelete, null -> dialog
            }
            current.copy(dialog = updatedDialog, message = null)
        }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    fun onConfirmAdd() {
        val dialog = _uiState.value.dialog as? OutlineEditDialog.Add ?: return
        if (_uiState.value.isOperating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, message = null) }
            when (addArticleSectionUseCase(articleId, dialog.heading)) {
                ArticleSectionOperationResult.Updated -> {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            dialog = null,
                            message = OutlineEditMessage.SectionAdded,
                        )
                    }
                }

                ArticleSectionOperationResult.InvalidHeading -> showDialogHeadingError()
                ArticleSectionOperationResult.NotPhase2OrMissing -> showOperationFailed()
            }
        }
    }

    fun onConfirmEditHeading() {
        val dialog = _uiState.value.dialog as? OutlineEditDialog.EditHeading ?: return
        if (_uiState.value.isOperating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, message = null) }
            when (
                updateArticleSectionHeadingUseCase(
                    articleId = articleId,
                    sectionId = dialog.sectionId,
                    heading = dialog.heading,
                )
            ) {
                ArticleSectionOperationResult.Updated -> {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            dialog = null,
                            message = OutlineEditMessage.SectionUpdated,
                        )
                    }
                }

                ArticleSectionOperationResult.InvalidHeading -> showDialogHeadingError()
                ArticleSectionOperationResult.NotPhase2OrMissing -> showOperationFailed()
            }
        }
    }

    fun onConfirmDelete() {
        val dialog = _uiState.value.dialog as? OutlineEditDialog.ConfirmDelete ?: return
        if (_uiState.value.isOperating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, message = null) }
            when (deleteArticleSectionUseCase(articleId, dialog.sectionId)) {
                DeleteArticleSectionResult.Deleted -> {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            dialog = null,
                            message = OutlineEditMessage.SectionDeleted,
                        )
                    }
                }

                DeleteArticleSectionResult.LastSection -> {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            dialog = null,
                            message = OutlineEditMessage.LastSectionCannotDelete,
                        )
                    }
                }

                DeleteArticleSectionResult.NotPhase2OrMissing -> showOperationFailed()
            }
        }
    }

    fun onMoveUpClick(sectionId: Long) {
        moveSection(sectionId, ArticleSectionMoveDirection.Up)
    }

    fun onMoveDownClick(sectionId: Long) {
        moveSection(sectionId, ArticleSectionMoveDirection.Down)
    }

    private fun moveSection(sectionId: Long, direction: ArticleSectionMoveDirection) {
        if (_uiState.value.isOperating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOperating = true, message = null) }
            when (moveArticleSectionUseCase(articleId, sectionId, direction)) {
                ArticleSectionOperationResult.Updated -> {
                    _uiState.update {
                        it.copy(
                            isOperating = false,
                            message = OutlineEditMessage.SectionMoved,
                        )
                    }
                }

                ArticleSectionOperationResult.InvalidHeading -> {
                    _uiState.update { it.copy(isOperating = false) }
                }

                ArticleSectionOperationResult.NotPhase2OrMissing -> showOperationFailed()
            }
        }
    }

    private fun showDialogHeadingError() {
        _uiState.update { current ->
            val updatedDialog = when (val dialog = current.dialog) {
                is OutlineEditDialog.Add -> dialog.copy(headingError = true)
                is OutlineEditDialog.EditHeading -> dialog.copy(headingError = true)
                is OutlineEditDialog.ConfirmDelete, null -> dialog
            }
            current.copy(
                isOperating = false,
                dialog = updatedDialog,
                message = OutlineEditMessage.HeadingRequired,
            )
        }
    }

    private fun showOperationFailed() {
        _uiState.update {
            it.copy(
                isOperating = false,
                message = OutlineEditMessage.OperationFailed,
            )
        }
    }
}

data class OutlineEditUiState(
    val title: String = "",
    val sections: List<OutlineEditSectionUiState> = emptyList(),
    val isLoading: Boolean = false,
    val isOperating: Boolean = false,
    val dialog: OutlineEditDialog? = null,
    val message: OutlineEditMessage? = null,
    val error: OutlineEditError? = null,
)

data class OutlineEditSectionUiState(
    val id: Long,
    val heading: String,
    val orderIndex: Int,
    val hasContent: Boolean,
    val userApproved: Boolean,
)

sealed interface OutlineEditDialog {
    data class Add(
        val heading: String,
        val headingError: Boolean = false,
    ) : OutlineEditDialog

    data class EditHeading(
        val sectionId: Long,
        val heading: String,
        val headingError: Boolean = false,
    ) : OutlineEditDialog

    data class ConfirmDelete(
        val sectionId: Long,
        val heading: String,
    ) : OutlineEditDialog
}

enum class OutlineEditMessage {
    SectionAdded,
    SectionUpdated,
    SectionDeleted,
    SectionMoved,
    HeadingRequired,
    LastSectionCannotDelete,
    OperationFailed,
}

enum class OutlineEditError {
    NotFound,
    NotPhase2,
}

private fun ArticleSection.toUiState(): OutlineEditSectionUiState =
    OutlineEditSectionUiState(
        id = id,
        heading = heading,
        orderIndex = orderIndex,
        hasContent = content.isNotBlank(),
        userApproved = userApproved,
    )
