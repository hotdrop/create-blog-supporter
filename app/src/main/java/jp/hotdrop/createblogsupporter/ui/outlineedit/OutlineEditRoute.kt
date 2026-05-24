package jp.hotdrop.createblogsupporter.ui.outlineedit

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OutlineEditRoute(
    onBack: () -> Unit,
    viewModel: OutlineEditViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    OutlineEditScreen(
        uiState = uiState.value,
        onBack = onBack,
        onAddClick = viewModel::onAddClick,
        onEditHeadingClick = viewModel::onEditHeadingClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDialogHeadingChanged = viewModel::onDialogHeadingChanged,
        onDismissDialog = viewModel::onDismissDialog,
        onConfirmAdd = viewModel::onConfirmAdd,
        onConfirmEditHeading = viewModel::onConfirmEditHeading,
        onConfirmDelete = viewModel::onConfirmDelete,
        onMoveUpClick = viewModel::onMoveUpClick,
        onMoveDownClick = viewModel::onMoveDownClick,
    )
}
