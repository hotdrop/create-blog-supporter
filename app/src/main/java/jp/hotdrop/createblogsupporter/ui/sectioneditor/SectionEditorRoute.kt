package jp.hotdrop.createblogsupporter.ui.sectioneditor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SectionEditorRoute(
    onBack: () -> Unit,
    onOpenConsultation: () -> Unit,
    viewModel: SectionEditorViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var showDiscardChangesDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.closeEvents.collectLatest {
            showDiscardChangesDialog = false
            onBack()
        }
    }

    val requestBack = {
        if (uiState.value.hasUnsavedChanges) {
            showDiscardChangesDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = !showDiscardChangesDialog, onBack = requestBack)

    SectionEditorScreen(
        uiState = uiState.value,
        onBack = requestBack,
        onDraftContentChanged = viewModel::onDraftContentChanged,
        onSaveContentClick = viewModel::onSaveContentClick,
        onUserApprovedChanged = viewModel::onUserApprovedChanged,
        onOpenConsultationClick = onOpenConsultation,
        onProofreadClick = viewModel::onProofreadClick,
        onCancelProofreadClick = viewModel::onCancelProofreadClick,
        showDiscardChangesDialog = showDiscardChangesDialog,
        onDismissDiscardChangesDialog = { showDiscardChangesDialog = false },
        onConfirmDiscardChanges = viewModel::onDiscardChangesClick,
    )
}
