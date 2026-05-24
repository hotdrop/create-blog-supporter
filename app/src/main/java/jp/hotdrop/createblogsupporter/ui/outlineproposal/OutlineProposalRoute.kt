package jp.hotdrop.createblogsupporter.ui.outlineproposal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OutlineProposalRoute(
    onBack: () -> Unit,
    onAdopted: (Long) -> Unit,
    viewModel: OutlineProposalViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.adoptedEvent.collect { articleId ->
            onAdopted(articleId)
        }
    }

    OutlineProposalScreen(
        uiState = uiState.value,
        onBack = onBack,
        onTitleSelected = viewModel::onTitleSelected,
        onOutlineSelected = viewModel::onOutlineSelected,
        onAdoptClick = viewModel::onAdoptClick,
    )
}
