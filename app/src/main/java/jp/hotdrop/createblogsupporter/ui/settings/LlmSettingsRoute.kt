package jp.hotdrop.createblogsupporter.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LlmSettingsRoute(
    onBack: () -> Unit,
    viewModel: LlmSettingsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        uri?.let(viewModel::onModelSelected)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LlmSettingsEffect.OpenModelPicker -> pickerLauncher.launch(arrayOf("*/*"))
            }
        }
    }

    LlmSettingsScreen(
        uiState = uiState.value,
        onBack = onBack,
        onPickModelClick = viewModel::onPickModelClick,
    )
}
