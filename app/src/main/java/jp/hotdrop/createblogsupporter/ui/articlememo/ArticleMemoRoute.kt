package jp.hotdrop.createblogsupporter.ui.articlememo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArticleMemoRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onGenerateOutline: (Long) -> Unit,
    viewModel: ArticleMemoViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.savedEvent.collect {
            onSaved()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.generateOutlineEvent.collect { articleId ->
            onGenerateOutline(articleId)
        }
    }

    ArticleMemoScreen(
        uiState = uiState.value,
        onBack = onBack,
        onTopicChanged = viewModel::onTopicChanged,
        onDetailChanged = viewModel::onDetailChanged,
        onGenerateOutlineClick = viewModel::onGenerateOutlineClick,
        onSaveClick = viewModel::onSaveClick,
    )
}
