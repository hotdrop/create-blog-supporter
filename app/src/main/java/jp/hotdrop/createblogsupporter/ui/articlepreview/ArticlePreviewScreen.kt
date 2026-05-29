package jp.hotdrop.createblogsupporter.ui.articlepreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun ArticlePreviewScreen(
    uiState: ArticlePreviewUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.full_preview_heading)) },
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

            else -> PreviewContent(
                uiState = uiState,
                innerPadding = innerPadding,
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
    error: ArticlePreviewError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        ArticlePreviewError.NotFound -> stringResource(R.string.unknown_article)
        ArticlePreviewError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
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
private fun PreviewContent(
    uiState: ArticlePreviewUiState,
    innerPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!uiState.hasSavedSectionContent) {
            Text(
                text = stringResource(R.string.article_preview_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = uiState.previewText,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articlePreview.content"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlePreviewReadyPreview() {
    CreateBlogSupporterTheme {
        ArticlePreviewScreen(
            uiState = ArticlePreviewUiState(
                title = "Compose Navigation を実装から理解する",
                previewText = """
                    # Compose Navigation を実装から理解する

                    ## 背景と解決したかったこと

                    保存済み本文だけを表示します。

                    ## 実装で詰まったポイント
                """.trimIndent(),
                hasSavedSectionContent = true,
            ),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlePreviewEmptyPreview() {
    CreateBlogSupporterTheme {
        ArticlePreviewScreen(
            uiState = ArticlePreviewUiState(
                title = "Compose Navigation を実装から理解する",
                previewText = "# Compose Navigation を実装から理解する",
            ),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlePreviewLoadingPreview() {
    CreateBlogSupporterTheme {
        ArticlePreviewScreen(
            uiState = ArticlePreviewUiState(isLoading = true),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlePreviewErrorPreview() {
    CreateBlogSupporterTheme {
        ArticlePreviewScreen(
            uiState = ArticlePreviewUiState(error = ArticlePreviewError.NotFound),
            onBack = {},
        )
    }
}
