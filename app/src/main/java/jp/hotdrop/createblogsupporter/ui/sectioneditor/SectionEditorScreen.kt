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
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingCheckResult
import jp.hotdrop.createblogsupporter.domain.model.ProofreadingIssue
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
    onOpenConsultationClick: () -> Unit,
    onProofreadClick: () -> Unit = {},
    onCancelProofreadClick: () -> Unit = {},
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
                onOpenConsultationClick = onOpenConsultationClick,
                onProofreadClick = onProofreadClick,
                onCancelProofreadClick = onCancelProofreadClick,
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
    onOpenConsultationClick: () -> Unit,
    onProofreadClick: () -> Unit,
    onCancelProofreadClick: () -> Unit,
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
        Text(
            text = stringResource(
                R.string.current_section_character_count_format,
                uiState.currentCharacterCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("sectionEditor.currentCharacterCount"),
        )
        Button(
            onClick = onOpenConsultationClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sectionEditor.openConsultationButton")
                .semantics { contentDescription = "open_section_consultation" },
        ) {
            Text(text = stringResource(R.string.open_section_consultation))
        }
        ProofreadingContent(
            uiState = uiState,
            onProofreadClick = onProofreadClick,
            onCancelProofreadClick = onCancelProofreadClick,
        )
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
private fun ProofreadingContent(
    uiState: SectionEditorUiState,
    onProofreadClick: () -> Unit,
    onCancelProofreadClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.section_proofreading_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.section_proofreading_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ProofreadingMessageText(message = uiState.proofreadingMessage)
        if (uiState.isProofreading) {
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
                                text = stringResource(R.string.section_proofreading_processing_title),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(R.string.section_proofreading_processing_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onCancelProofreadClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sectionEditor.cancelProofreadButton")
                            .semantics { contentDescription = "cancel_section_proofreading" },
                    ) {
                        Text(text = stringResource(R.string.section_proofreading_cancel))
                    }
                }
            }
        } else {
            Button(
                onClick = onProofreadClick,
                enabled = uiState.currentCharacterCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sectionEditor.proofreadButton")
                    .semantics { contentDescription = "check_section_proofreading" },
            ) {
                Text(text = stringResource(R.string.section_proofreading_check))
            }
        }
        uiState.proofreadingResult?.let { result ->
            ProofreadingResultCard(result = result)
        }
    }
}

@Composable
private fun ProofreadingResultCard(result: ProofreadingCheckResult) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("sectionEditor.proofreadingResult"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.section_proofreading_result_heading),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (result.issues.isEmpty()) {
                Text(
                    text = stringResource(R.string.section_proofreading_no_issues),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                result.issues.forEach { issue ->
                    ProofreadingIssueContent(issue = issue)
                }
            }
        }
    }
}

@Composable
private fun ProofreadingIssueContent(issue: ProofreadingIssue) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sectionEditor.proofreadingIssue"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.section_proofreading_issue_target, issue.targetText),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.section_proofreading_issue_suggestion, issue.suggestion),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.section_proofreading_issue_reason, issue.reason),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
private fun ProofreadingMessageText(message: SectionEditorProofreadingMessage?) {
    val text = when (message) {
        null -> null
        SectionEditorProofreadingMessage.ModelNotConfigured -> {
            stringResource(R.string.section_proofreading_model_not_configured)
        }
        SectionEditorProofreadingMessage.ModelInitializationFailed -> {
            stringResource(R.string.section_proofreading_model_initialization_failed)
        }
        SectionEditorProofreadingMessage.CheckFailed -> stringResource(R.string.section_proofreading_check_failed)
        SectionEditorProofreadingMessage.Cancelled -> stringResource(R.string.section_proofreading_cancelled)
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
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorProofreadingPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(isProofreading = true),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onOpenConsultationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorProofreadingResultPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                proofreadingResult = ProofreadingCheckResult(
                    message = "誤字脱字・表記ゆれの候補が 1 件あります。必要なものだけ反映してください。",
                    issues = listOf(
                        ProofreadingIssue(
                            id = "proofreading-1",
                            targetText = "LiteRM",
                            suggestion = "LiteRT-LM",
                            reason = "SDK名の表記をプロジェクト内の正式表記にそろえる候補です。",
                        ),
                    ),
                ),
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onOpenConsultationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorProofreadingNoIssuesPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                proofreadingResult = ProofreadingCheckResult(
                    message = "候補は見つかりませんでした。",
                    issues = emptyList(),
                ),
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onOpenConsultationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionEditorProofreadingModelNotConfiguredPreview() {
    CreateBlogSupporterTheme {
        SectionEditorScreen(
            uiState = PreviewState.copy(
                proofreadingMessage = SectionEditorProofreadingMessage.ModelNotConfigured,
            ),
            onBack = {},
            onDraftContentChanged = {},
            onSaveContentClick = {},
            onUserApprovedChanged = {},
            onOpenConsultationClick = {},
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
            onOpenConsultationClick = {},
            showDiscardChangesDialog = true,
        )
    }
}
