package jp.hotdrop.createblogsupporter.ui.sectioneditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Switch
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
fun SectionEditorScreen(
    uiState: SectionEditorUiState,
    onBack: () -> Unit,
    onDraftContentChanged: (String) -> Unit,
    onSaveContentClick: () -> Unit,
    onResetDraftClick: () -> Unit,
    onUserApprovedChanged: (Boolean) -> Unit,
    onConsultationInputChanged: (String) -> Unit,
    onAskLlmClick: () -> Unit,
    onCancelLlmClick: () -> Unit,
    onCopyConsultationAnswerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.section_editor_title)) },
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

            else -> SectionEditorContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onDraftContentChanged = onDraftContentChanged,
                onSaveContentClick = onSaveContentClick,
                onResetDraftClick = onResetDraftClick,
                onUserApprovedChanged = onUserApprovedChanged,
                onConsultationInputChanged = onConsultationInputChanged,
                onAskLlmClick = onAskLlmClick,
                onCancelLlmClick = onCancelLlmClick,
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
    error: SectionEditorError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        SectionEditorError.NotFound -> stringResource(R.string.unknown_article)
        SectionEditorError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
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
private fun SectionEditorContent(
    uiState: SectionEditorUiState,
    innerPadding: PaddingValues,
    onDraftContentChanged: (String) -> Unit,
    onSaveContentClick: () -> Unit,
    onResetDraftClick: () -> Unit,
    onUserApprovedChanged: (Boolean) -> Unit,
    onConsultationInputChanged: (String) -> Unit,
    onAskLlmClick: () -> Unit,
    onCancelLlmClick: () -> Unit,
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
        MessageText(message = uiState.message)
        SavedContent(content = uiState.content)
        OutlinedTextField(
            value = uiState.draftContent,
            onValueChange = onDraftContentChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionEditor.draftInput")
                .semantics { contentDescription = "section_draft_content_input" },
            label = { Text(text = stringResource(R.string.section_draft_content_label)) },
            minLines = 10,
        )
        Text(
            text = stringResource(
                R.string.current_section_character_count_format,
                uiState.currentCharacterCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("sectionEditor.currentCharacterCount"),
        )
        LlmConsultationContent(
            uiState = uiState,
            onConsultationInputChanged = onConsultationInputChanged,
            onAskLlmClick = onAskLlmClick,
            onCancelLlmClick = onCancelLlmClick,
            onCopyConsultationAnswerClick = onCopyConsultationAnswerClick,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onSaveContentClick,
                enabled = !uiState.isSavingContent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("sectionEditor.saveButton")
                    .semantics { contentDescription = "save_section_content" },
            ) {
                if (uiState.isSavingContent) {
                    CircularProgressIndicator()
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Text(
                        text = stringResource(R.string.save_section_content),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            OutlinedButton(
                onClick = onResetDraftClick,
                enabled = !uiState.isResettingDraft,
                modifier = Modifier
                    .weight(1f)
                    .testTag("sectionEditor.resetButton")
                    .semantics { contentDescription = "reset_section_draft" },
            ) {
                if (uiState.isResettingDraft) {
                    CircularProgressIndicator()
                } else {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    Text(
                        text = stringResource(R.string.reset_section_draft),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        Row(

            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.section_user_approved),
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = uiState.userApproved,
                onCheckedChange = onUserApprovedChanged,
                enabled = !uiState.isUpdatingApproval,
                modifier = Modifier
                    .testTag("sectionEditor.approvedSwitch")
                    .semantics { contentDescription = "section_user_approved_switch" },
            )
        }
    }
}

@Composable
private fun LlmConsultationContent(
    uiState: SectionEditorUiState,
    onConsultationInputChanged: (String) -> Unit,
    onAskLlmClick: () -> Unit,
    onCancelLlmClick: () -> Unit,
    onCopyConsultationAnswerClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.section_llm_consultation_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.section_llm_consultation_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = uiState.consultationInput,
            onValueChange = onConsultationInputChanged,
            enabled = !uiState.isConsultingLlm,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionEditor.consultationInput")
                .semantics { contentDescription = "section_llm_consultation_input" },
            label = { Text(text = stringResource(R.string.section_llm_consultation_label)) },
            placeholder = { Text(text = stringResource(R.string.section_llm_consultation_placeholder)) },
            minLines = 3,
        )
        ConsultationMessageText(message = uiState.consultationMessage)
        if (uiState.isConsultingLlm) {
            Card(shape = RoundedCornerShape(8.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Column {
                            Text(
                                text = stringResource(R.string.section_llm_processing_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(R.string.section_llm_processing_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onCancelLlmClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sectionEditor.cancelLlmButton")
                            .semantics { contentDescription = "cancel_section_llm_consultation" },
                    ) {
                        Text(text = stringResource(R.string.section_llm_cancel))
                    }
                }
            }
        } else {
            Button(
                onClick = onAskLlmClick,
                enabled = uiState.consultationInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sectionEditor.askLlmButton")
                    .semantics { contentDescription = "ask_section_llm" },
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                Text(
                    text = stringResource(R.string.section_llm_ask),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (uiState.consultationAnswer.isNotBlank()) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("sectionEditor.consultationAnswer"),
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
                            .testTag("sectionEditor.copyConsultationButton")
                            .semantics { contentDescription = "copy_section_llm_answer" },
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
private fun SavedContent(content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.section_saved_content_label),
            style = MaterialTheme.typography.titleMedium,
        )
        Card(shape = RoundedCornerShape(8.dp)) {
            Text(
                text = content.ifBlank { stringResource(R.string.section_saved_content_empty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageText(message: SectionEditorMessage?) {
    val text = when (message) {
        null -> null
        SectionEditorMessage.DraftAutoSaved -> null
        SectionEditorMessage.ContentSaved -> stringResource(R.string.section_content_saved_message)
        SectionEditorMessage.DraftReset -> stringResource(R.string.section_draft_reset_message)
        SectionEditorMessage.MarkedApproved -> stringResource(R.string.section_marked_approved)
        SectionEditorMessage.MarkedUnapproved -> stringResource(R.string.section_marked_unapproved)
        SectionEditorMessage.OperationFailed -> stringResource(R.string.section_operation_failed)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsultationMessageText(message: SectionEditorConsultationMessage?) {
    val text = when (message) {
        null -> null
        SectionEditorConsultationMessage.EmptyQuestion -> stringResource(R.string.section_llm_empty_question)
        SectionEditorConsultationMessage.ModelNotConfigured -> stringResource(R.string.section_llm_model_not_configured)
        SectionEditorConsultationMessage.ModelInitializationFailed -> {
            stringResource(R.string.section_llm_model_initialization_failed)
        }
        SectionEditorConsultationMessage.GenerationFailed -> stringResource(R.string.section_llm_generation_failed)
        SectionEditorConsultationMessage.Cancelled -> stringResource(R.string.section_llm_cancelled)
        SectionEditorConsultationMessage.Copied -> stringResource(R.string.section_llm_copied)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PreviewState = SectionEditorUiState(
    articleTitle = "Compose Navigationの設計判断",
    topic = "Navigation Composeで画面遷移を整理した話",
    detail = "RouteとScreenを分けた理由、実装で詰まった点、次に改善したい点を書く。",
    heading = "背景と解決したかったこと",
    orderIndex = 0,
    content = "保存済み本文です。\nここまではユーザーが確認済みです。",
    draftContent = "保存済み本文です。\nここに追記中です。",
    userApproved = false,
)

@Preview(showBackground = true, heightDp = 970)
@Composable
private fun SectionEditorReadyPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState,
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorLoadingPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = SectionEditorUiState(isLoading = true),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorNotFoundPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = SectionEditorUiState(error = SectionEditorError.NotFound),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorNotPhase2Preview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = SectionEditorUiState(error = SectionEditorError.NotPhase2),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorSavingPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(isSavingContent = true),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorEmptyContentPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = SectionEditorUiState(
                heading = "まとめ",
                orderIndex = 2,
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultationInputPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "この章では何を書けばいいでしょうか？",
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultingPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "雑なメモを記事向けに整理する観点をください。",
                isConsultingLlm = true,
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultationAnswerPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "この章では何を書けばいいでしょうか？",
                consultationAnswer = "背景では、最初に困っていたこと、既存の構成で読みづらかった点、RouteとScreenを分ける判断につながった理由を順に整理すると書きやすいです。",
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultationErrorPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "清書の方向性を相談したいです。",
                consultationMessage = SectionEditorConsultationMessage.ModelNotConfigured,
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onResetDraftClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onAskLlmClick = {},
            onCancelLlmClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}
