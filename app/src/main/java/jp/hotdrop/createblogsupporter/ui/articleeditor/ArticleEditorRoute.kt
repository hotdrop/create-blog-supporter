package jp.hotdrop.createblogsupporter.ui.articleeditor

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArticleEditorRoute(
    onBack: () -> Unit,
    onEditOutline: (Long) -> Unit,
    viewModel: ArticleEditorViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    ArticleEditorScreen(
        uiState = uiState.value,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onSaveTitleClick = viewModel::onSaveTitleClick,
        onEditOutlineClick = { articleId ->
            onEditOutline(articleId)
        },
        onSectionClick = viewModel::onSectionClick,
    )
}
