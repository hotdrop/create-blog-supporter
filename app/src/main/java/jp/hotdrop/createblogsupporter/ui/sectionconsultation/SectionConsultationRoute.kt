package jp.hotdrop.createblogsupporter.ui.sectionconsultation

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
fun SectionConsultationRoute(
    onBack: () -> Unit,
    viewModel: SectionConsultationViewModel = hiltViewModel(),
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

    SectionConsultationScreen(
        uiState = uiState.value,
        onBack = onBack,
        onConsultationInputChanged = viewModel::onConsultationInputChanged,
        onCreatePastePromptClick = viewModel::onCreatePastePromptClick,
        onCopyConsultationAnswerClick = viewModel::onCopyConsultationAnswerClick,
    )
}
