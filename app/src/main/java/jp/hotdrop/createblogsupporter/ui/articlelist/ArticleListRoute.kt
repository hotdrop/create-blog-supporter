package jp.hotdrop.createblogsupporter.ui.articlelist

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArticleListRoute(
    onCreateArticle: () -> Unit,
    onOpenArticle: (Long) -> Unit,
    viewModel: ArticleListViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    ArticleListScreen(
        uiState = uiState.value,
        onCreateArticle = onCreateArticle,
        onOpenArticle = onOpenArticle,
    )
}
