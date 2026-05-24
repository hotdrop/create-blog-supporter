package jp.hotdrop.createblogsupporter.ui.sectioneditor

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SectionEditorRoute(
    onBack: () -> Unit,
    viewModel: SectionEditorViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    SectionEditorScreen(
        uiState = uiState.value,
        onBack = onBack,
        onDraftContentChanged = viewModel::onDraftContentChanged,
        onSaveContentClick = viewModel::onSaveContentClick,
        onResetDraftClick = viewModel::onResetDraftClick,
        onUserApprovedChanged = viewModel::onUserApprovedChanged,
        onToggleComparisonClick = viewModel::onToggleComparisonClick,
    )
}
