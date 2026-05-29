package jp.hotdrop.createblogsupporter.ui.articlememo

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.createblogsupporter.R
import jp.hotdrop.createblogsupporter.ui.theme.CreateBlogSupporterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleMemoScreen(
    uiState: ArticleMemoUiState,
    onBack: () -> Unit,
    onTopicChanged: (String) -> Unit,
    onDetailChanged: (String) -> Unit,
    onGenerateOutlineClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.mode) {
                            ArticleMemoMode.New -> stringResource(R.string.new_article_title)
                            ArticleMemoMode.Edit -> stringResource(R.string.edit_article_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "navigate_back"
                        },
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
        } else if (uiState.error == ArticleMemoError.NotFound) {
            MissingArticleContent(
                innerPadding = innerPadding,
                onBack = onBack,
            )
        } else {
            ArticleMemoForm(
                uiState = uiState,
                innerPadding = innerPadding,
                onTopicChanged = onTopicChanged,
                onDetailChanged = onDetailChanged,
                onGenerateOutlineClick = onGenerateOutlineClick,
                onSaveClick = onSaveClick,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun LoadingContent(
    innerPadding: PaddingValues,
) {
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
private fun MissingArticleContent(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.unknown_article),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) {
            Text(text = stringResource(R.string.navigate_back))
        }
    }
}

@Composable
private fun ArticleMemoForm(
    uiState: ArticleMemoUiState,
    innerPadding: PaddingValues,
    onTopicChanged: (String) -> Unit,
    onDetailChanged: (String) -> Unit,
    onGenerateOutlineClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = uiState.topic,
            onValueChange = onTopicChanged,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "topic_input" },
            label = { Text(text = stringResource(R.string.topic_label)) },
            isError = uiState.topicError,
            supportingText = {
                if (uiState.topicError) {
                    Text(text = stringResource(R.string.topic_required))
                }
            },
            singleLine = false,
            minLines = 2,
        )
        OutlinedTextField(
            value = uiState.detail,
            onValueChange = onDetailChanged,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "detail_input" },
            label = { Text(text = stringResource(R.string.detail_label)) },
            singleLine = false,
            minLines = 6,
        )
        Spacer(modifier = Modifier.height(16.dp))
        MessageText(message = uiState.message)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onGenerateOutlineClick,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "generate_outline" },
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.generate_outline),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TextButton(
                onClick = onBack,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "save_article" },
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.save),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageText(message: ArticleMemoMessage?) {
    val text = when (message) {
        null -> null
        ArticleMemoMessage.TopicRequired -> stringResource(R.string.topic_required)
        ArticleMemoMessage.Saved -> stringResource(R.string.article_saved)
        ArticleMemoMessage.SaveFailed -> stringResource(R.string.article_save_failed)
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
private fun ArticleMemoNewPreview() {
    CreateBlogSupporterTheme {
        ArticleMemoScreen(
            uiState = ArticleMemoUiState(mode = ArticleMemoMode.New),
            onBack = {},
            onTopicChanged = {},
            onDetailChanged = {},
            onGenerateOutlineClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleMemoValidationPreview() {
    CreateBlogSupporterTheme {
        ArticleMemoScreen(
            uiState = ArticleMemoUiState(
                mode = ArticleMemoMode.New,
                topicError = true,
                message = ArticleMemoMessage.TopicRequired,
            ),
            onBack = {},
            onTopicChanged = {},
            onDetailChanged = {},
            onGenerateOutlineClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleMemoEditPreview() {
    CreateBlogSupporterTheme {
        ArticleMemoScreen(
            uiState = ArticleMemoUiState(
                mode = ArticleMemoMode.Edit,
                topic = "Compose Navigationの設計",
                detail = "画面とRouteを分ける理由を、実装例と一緒に整理する。",
                message = ArticleMemoMessage.Saved,
            ),
            onBack = {},
            onTopicChanged = {},
            onDetailChanged = {},
            onGenerateOutlineClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleMemoLoadingPreview() {
    CreateBlogSupporterTheme {
        ArticleMemoScreen(
            uiState = ArticleMemoUiState(
                mode = ArticleMemoMode.Edit,
                isLoading = true,
            ),
            onBack = {},
            onTopicChanged = {},
            onDetailChanged = {},
            onGenerateOutlineClick = {},
            onSaveClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleMemoMissingPreview() {
    CreateBlogSupporterTheme {
        ArticleMemoScreen(
            uiState = ArticleMemoUiState(
                mode = ArticleMemoMode.Edit,
                error = ArticleMemoError.NotFound,
            ),
            onBack = {},
            onTopicChanged = {},
            onDetailChanged = {},
            onGenerateOutlineClick = {},
            onSaveClick = {},
        )
    }
}
