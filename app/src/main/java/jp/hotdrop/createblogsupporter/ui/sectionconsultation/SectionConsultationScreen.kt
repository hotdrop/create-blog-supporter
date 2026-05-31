package jp.hotdrop.createblogsupporter.ui.sectionconsultation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
fun SectionConsultationScreen(
    modifier: Modifier = Modifier,
    uiState: SectionConsultationUiState,
    onBack: () -> Unit,
    onConsultationInputChanged: (String) -> Unit,
    onCreatePastePromptClick: () -> Unit,
    onCopyConsultationAnswerClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.section_consultation_title)) },
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

            else -> ConsultationContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onConsultationInputChanged = onConsultationInputChanged,
                onCreatePastePromptClick = onCreatePastePromptClick,
                onCopyConsultationAnswerClick = onCopyConsultationAnswerClick,
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
    error: SectionConsultationError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        SectionConsultationError.NotFound -> stringResource(R.string.unknown_article)
        SectionConsultationError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
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
private fun ConsultationContent(
    uiState: SectionConsultationUiState,
    innerPadding: PaddingValues,
    onConsultationInputChanged: (String) -> Unit,
    onCreatePastePromptClick: () -> Unit,
    onCopyConsultationAnswerClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(
                R.string.section_editor_heading_format,
                uiState.orderIndex + 1,
                uiState.heading,
            ),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.section_llm_consultation_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = uiState.consultationInput,
            onValueChange = onConsultationInputChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionConsultation.input")
                .semantics { contentDescription = "section_chatgpt_request_note_input" },
            label = { Text(text = stringResource(R.string.section_llm_consultation_label)) },
            placeholder = { Text(text = stringResource(R.string.section_llm_consultation_placeholder)) },
            minLines = 3,
        )
        ConsultationMessageText(message = uiState.message)
        Button(
            onClick = onCreatePastePromptClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionConsultation.createButton")
                .semantics { contentDescription = "create_section_chatgpt_request" },
        ) {
            Text(text = stringResource(R.string.section_llm_ask))
        }
        if (uiState.consultationAnswer.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("sectionConsultation.result"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.section_llm_answer_heading),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = uiState.consultationAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedButton(
                        onClick = onCopyConsultationAnswerClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sectionConsultation.copyButton")
                            .semantics { contentDescription = "copy_section_chatgpt_request" },
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Text(
                            text = stringResource(R.string.section_llm_copy_answer),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsultationMessageText(message: SectionConsultationMessage?) {
    val text = when (message) {
        null -> null
        SectionConsultationMessage.Copied -> stringResource(R.string.section_llm_copied)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PreviewState = SectionConsultationUiState(
    articleTitle = "Compose Navigationの設計判断",
    topic = "Navigation Composeで画面遷移を整理した話",
    detail = "RouteとScreenを分けた理由、実装で詰まった点、次に改善したい点を書く。",
    heading = "背景と解決したかったこと",
    orderIndex = 0,
    content = "保存済み本文です。\nここまではユーザーが確認済みです。",
    draftContent = "保存済み本文です。\nここに追記中です。",
)

@Preview(showBackground = true)
@Composable
private fun SectionConsultationReadyPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = PreviewState,
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationInputPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = PreviewState.copy(
                consultationInput = "実装で困ったところも自然に入れたいです。",
            ),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationResultPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = PreviewState.copy(
                consultationInput = "実装で困ったところも自然に入れたいです。",
                consultationAnswer = "ChatGPTへの依頼:\n以下の文脈をもとに、「現在の章」の完成本文案をテックブログ向けの自然な文章として作成してください。\n\n記事タイトル:\nCompose Navigationの設計判断",
            ),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationCopiedPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = PreviewState.copy(
                consultationAnswer = "ChatGPTへの依頼:\n現在の章の完成本文案を作成してください。",
                message = SectionConsultationMessage.Copied,
            ),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationLoadingPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = SectionConsultationUiState(isLoading = true),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationNotFoundPreview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = SectionConsultationUiState(error = SectionConsultationError.NotFound),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionConsultationNotPhase2Preview() {
    CreateBlogSupporterTheme {
        SectionConsultationScreen(
            uiState = SectionConsultationUiState(error = SectionConsultationError.NotPhase2),
            onBack = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}
