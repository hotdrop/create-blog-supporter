package jp.hotdrop.createblogsupporter.ui.sectioneditor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.hotdrop.createblogsupporter.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SectionEditorRoute(
    onBack: () -> Unit,
    viewModel: SectionEditorViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDiscardChangesDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.copyEvents.collectLatest { text ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    context.getString(R.string.section_llm_clipboard_label),
                    text,
                ),
            )
        }
    }

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
        onConsultationInputChanged = viewModel::onConsultationInputChanged,
        onAskLlmClick = viewModel::onAskLlmClick,
        onCancelLlmClick = viewModel::onCancelLlmClick,
        onCopyConsultationAnswerClick = viewModel::onCopyConsultationAnswerClick,
        showDiscardChangesDialog = showDiscardChangesDialog,
        onDismissDiscardChangesDialog = { showDiscardChangesDialog = false },
        onConfirmDiscardChanges = viewModel::onDiscardChangesClick,
    )
}
