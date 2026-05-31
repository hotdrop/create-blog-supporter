package jp.hotdrop.createblogsupporter.ui.articletitleedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
fun ArticleTitleEditScreen(
    uiState: ArticleTitleEditUiState,
    onBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.article_title_edit_title)) },
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
        when {
            uiState.isLoading -> LoadingContent(innerPadding)
            uiState.error != null -> ErrorContent(
                innerPadding = innerPadding,
                error = uiState.error,
                onBack = onBack,
            )

            else -> ArticleTitleEditContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onTitleChanged = onTitleChanged,
                onSaveClick = onSaveClick,
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
private fun ErrorContent(
    innerPadding: PaddingValues,
    error: ArticleTitleEditError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        ArticleTitleEditError.NotFound -> stringResource(R.string.unknown_article)
        ArticleTitleEditError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) {
            Text(text = stringResource(R.string.navigate_back))
        }
    }
}

@Composable
private fun ArticleTitleEditContent(
    uiState: ArticleTitleEditUiState,
    innerPadding: PaddingValues,
    onTitleChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleTitleEdit.titleInput")
                .semantics { contentDescription = "article_title_input" },
            label = { Text(text = stringResource(R.string.article_title_label)) },
            isError = uiState.titleError,
            supportingText = {
                if (uiState.titleError) {
                    Text(text = stringResource(R.string.article_title_required))
                }
            },
            singleLine = false,
            minLines = 1,
        )
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleTitleEdit.saveButton")
                .semantics { contentDescription = "save_article_title" },
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Text(
                    text = stringResource(R.string.save_title),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        MessageText(message = uiState.message)
    }
}

@Composable
private fun MessageText(message: ArticleTitleEditMessage?) {
    val text = when (message) {
        null -> null
        ArticleTitleEditMessage.TitleRequired -> stringResource(R.string.article_title_required)
        ArticleTitleEditMessage.SaveFailed -> stringResource(R.string.article_title_save_failed)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditReadyPreview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(
                title = "Compose Navigation を実装から理解する",
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditSavingPreview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(
                title = "Compose Navigation を実装から理解する",
                isSaving = true,
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditTitleErrorPreview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(
                title = "",
                titleError = true,
                message = ArticleTitleEditMessage.TitleRequired,
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditLoadingPreview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(isLoading = true),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditNotFoundPreview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(error = ArticleTitleEditError.NotFound),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleTitleEditNotPhase2Preview() {
    CreateBlogSupporterTheme {
        ArticleTitleEditScreen(
            uiState = ArticleTitleEditUiState(error = ArticleTitleEditError.NotPhase2),
            onBack = {},
            onTitleChanged = {},
            onSaveClick = {},
        )
    }
}
