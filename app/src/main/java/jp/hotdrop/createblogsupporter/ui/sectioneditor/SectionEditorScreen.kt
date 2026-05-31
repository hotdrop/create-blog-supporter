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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
    modifier: Modifier = Modifier,
    uiState: SectionEditorUiState,
    onBack: () -> Unit,
    onDraftContentChanged: (String) -> Unit,
    onSaveContentClick: () -> Unit,
    onUserApprovedChanged: (Boolean) -> Unit,
    onConsultationInputChanged: (String) -> Unit,
    onCreatePastePromptClick: () -> Unit,
    onCopyConsultationAnswerClick: () -> Unit,
    showDiscardChangesDialog: Boolean = false,
    onDismissDiscardChangesDialog: () -> Unit = {},
    onConfirmDiscardChanges: () -> Unit = {}
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
                onUserApprovedChanged = onUserApprovedChanged,
                onConsultationInputChanged = onConsultationInputChanged,
                onCreatePastePromptClick = onCreatePastePromptClick,
                onCopyConsultationAnswerClick = onCopyConsultationAnswerClick,
            )
        }
    }
    if (showDiscardChangesDialog) {
        DiscardChangesDialog(
            isDiscardingChanges = uiState.isDiscardingChanges,
            onDismissRequest = onDismissDiscardChangesDialog,
            onConfirmClick = onConfirmDiscardChanges,
        )
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
    onUserApprovedChanged: (Boolean) -> Unit,
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
        MessageText(message = uiState.message)
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
            onCreatePastePromptClick = onCreatePastePromptClick,
            onCopyConsultationAnswerClick = onCopyConsultationAnswerClick,
        )
        Button(
            onClick = onSaveContentClick,
            enabled = !uiState.isSavingContent,
            modifier = Modifier
                .fillMaxWidth()
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
    onCreatePastePromptClick: () -> Unit,
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionEditor.consultationInput")
                .semantics { contentDescription = "section_chatgpt_request_note_input" },
            label = { Text(text = stringResource(R.string.section_llm_consultation_label)) },
            placeholder = { Text(text = stringResource(R.string.section_llm_consultation_placeholder)) },
            minLines = 3,
        )
        ConsultationMessageText(message = uiState.consultationMessage)
        Button(
            onClick = onCreatePastePromptClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionEditor.createPastePromptButton")
                .semantics { contentDescription = "create_section_chatgpt_request" },
        ) {
            Text(text = stringResource(R.string.section_llm_ask))
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
private fun MessageText(message: SectionEditorMessage?) {
    val text = when (message) {
        null -> null
        SectionEditorMessage.DraftAutoSaved -> null
        SectionEditorMessage.ContentSaved -> stringResource(R.string.section_content_saved_message)
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
private fun DiscardChangesDialog(
    isDiscardingChanges: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDiscardingChanges) {
                onDismissRequest()
            }
        },
        title = { Text(text = stringResource(R.string.section_discard_changes_title)) },
        text = { Text(text = stringResource(R.string.section_discard_changes_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirmClick,
                enabled = !isDiscardingChanges,
                modifier = Modifier
                    .testTag("sectionEditor.discardDialog.confirmButton")
                    .semantics { contentDescription = "discard_section_changes_and_close" },
            ) {
                Text(text = stringResource(R.string.section_discard_changes_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isDiscardingChanges,
                modifier = Modifier
                    .testTag("sectionEditor.discardDialog.cancelButton")
                    .semantics { contentDescription = "cancel_discard_section_changes" },
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        modifier = Modifier.testTag("sectionEditor.discardDialog"),
    )
}

@Composable
private fun ConsultationMessageText(message: SectionEditorConsultationMessage?) {
    val text = when (message) {
        null -> null
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
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
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorDiscardChangesDialogPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState,
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
            showDiscardChangesDialog = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultationAnswerPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "実装で困ったところも自然に入れたいです。",
                consultationAnswer = "ChatGPTへの依頼:\n以下の文脈をもとに、「現在の章」の完成本文案をテックブログ向けの自然な文章として作成してください。\n\n記事タイトル:\nCompose Navigationの設計判断",
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorConsultationCopiedPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                consultationInput = "清書の方向性を相談したいです。",
                consultationAnswer = "ChatGPTへの依頼:\n現在の章の完成本文案を作成してください。",
                consultationMessage = SectionEditorConsultationMessage.Copied,
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onConsultationInputChanged = {},
            onCreatePastePromptClick = {},
            onCopyConsultationAnswerClick = {},
        )
    }
}
