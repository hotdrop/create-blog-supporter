package jp.hotdrop.createblogsupporter.ui.articletitleedit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ArticleTitleEditRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ArticleTitleEditViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ArticleTitleEditEvent.Saved -> onSaved()
            }
        }
    }

    ArticleTitleEditScreen(
        uiState = uiState.value,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onSaveClick = viewModel::onSaveClick,
    )
}
