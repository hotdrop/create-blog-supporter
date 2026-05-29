package jp.hotdrop.createblogsupporter.ui.articlepreview

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArticlePreviewRoute(
    onBack: () -> Unit,
    viewModel: ArticlePreviewViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    ArticlePreviewScreen(
        uiState = uiState.value,
        onBack = onBack,
    )
}
