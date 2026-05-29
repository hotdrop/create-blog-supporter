package jp.hotdrop.createblogsupporter.ui.sectioneditor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    SectionEditorScreen(
        uiState = uiState.value,
        onBack = onBack,
        onDraftContentChanged = viewModel::onDraftContentChanged,
        onSaveContentClick = viewModel::onSaveContentClick,
        onResetDraftClick = viewModel::onResetDraftClick,
        onUserApprovedChanged = viewModel::onUserApprovedChanged,
        onToggleComparisonClick = viewModel::onToggleComparisonClick,
        onConsultationInputChanged = viewModel::onConsultationInputChanged,
        onAskLlmClick = viewModel::onAskLlmClick,
        onCancelLlmClick = viewModel::onCancelLlmClick,
        onCopyConsultationAnswerClick = viewModel::onCopyConsultationAnswerClick,
    )
}
