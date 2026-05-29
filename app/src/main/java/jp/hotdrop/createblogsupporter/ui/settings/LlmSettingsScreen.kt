package jp.hotdrop.createblogsupporter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.createblogsupporter.R
import jp.hotdrop.createblogsupporter.ui.theme.CreateBlogSupporterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmSettingsScreen(
    uiState: LlmSettingsUiState,
    onBack: () -> Unit,
    onPickModelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.llm_settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "navigate_back" },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingContent(innerPadding = innerPadding)
        } else {
            SettingsContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onPickModelClick = onPickModelClick,
            )
        }
    }
}

@Composable
private fun LoadingContent(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SettingsContent(
    uiState: LlmSettingsUiState,
    innerPadding: PaddingValues,
    onPickModelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(R.string.llm_settings_model_section_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = stringResource(R.string.llm_settings_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = uiState.modelDisplayName
                        ?: stringResource(R.string.llm_settings_model_not_selected),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag("llmSettings.selectedModelName"),
                )
                Text(
                    text = uiState.modelFilePath
                        ?: stringResource(R.string.llm_settings_model_path_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (uiState.isImportingModel) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("llmSettings.importProgress"),
                        )
                        Text(
                            text = stringResource(R.string.llm_settings_model_importing_detail),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Button(
                    onClick = onPickModelClick,
                    enabled = !uiState.isImportingModel,
                    modifier = Modifier.testTag("llmSettings.pickModelButton"),
                ) {
                    if (uiState.isImportingModel) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(R.string.llm_settings_pick_model),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
        MessageText(
            messageResId = uiState.messageResId,
            errorMessageResId = uiState.errorMessageResId,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MessageText(
    messageResId: Int?,
    errorMessageResId: Int?,
) {
    val text = when {
        errorMessageResId != null -> stringResource(errorMessageResId)
        messageResId != null -> stringResource(messageResId)
        else -> null
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (errorMessageResId != null) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LlmSettingsNoModelPreview() {
    CreateBlogSupporterTheme {
        LlmSettingsScreen(
            uiState = LlmSettingsUiState(),
            onBack = {},
            onPickModelClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LlmSettingsSelectedModelPreview() {
    CreateBlogSupporterTheme {
        LlmSettingsScreen(
            uiState = LlmSettingsUiState(
                modelDisplayName = "gemma-blog-support.litertlm",
                modelFilePath = "/data/user/0/jp.hotdrop.createblogsupporter/files/litertlm-models/gemma-blog-support.litertlm",
                messageResId = R.string.llm_settings_model_selected,
            ),
            onBack = {},
            onPickModelClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LlmSettingsImportingPreview() {
    CreateBlogSupporterTheme {
        LlmSettingsScreen(
            uiState = LlmSettingsUiState(
                isImportingModel = true,
                messageResId = R.string.llm_settings_model_importing,
            ),
            onBack = {},
            onPickModelClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LlmSettingsErrorPreview() {
    CreateBlogSupporterTheme {
        LlmSettingsScreen(
            uiState = LlmSettingsUiState(
                errorMessageResId = R.string.llm_settings_error_model_import_failed,
            ),
            onBack = {},
            onPickModelClick = {},
        )
    }
}
