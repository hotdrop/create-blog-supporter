package jp.hotdrop.createblogsupporter.ui.articleeditor

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
fun ArticleEditorScreen(
    uiState: ArticleEditorUiState,
    onBack: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onSaveTitleClick: () -> Unit,
    onExportMarkdownClick: () -> Unit,
    onEditOutlineClick: (Long) -> Unit,
    onOpenPreviewClick: (Long) -> Unit,
    onSectionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.article_editor_title)) },
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

            else -> ArticleEditorContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onTitleChanged = onTitleChanged,
                onSaveTitleClick = onSaveTitleClick,
                onExportMarkdownClick = onExportMarkdownClick,
                onEditOutlineClick = onEditOutlineClick,
                onOpenPreviewClick = onOpenPreviewClick,
                onSectionClick = onSectionClick,
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
    error: ArticleEditorError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        ArticleEditorError.NotFound -> stringResource(R.string.unknown_article)
        ArticleEditorError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
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
private fun ArticleEditorContent(
    uiState: ArticleEditorUiState,
    innerPadding: PaddingValues,
    onTitleChanged: (String) -> Unit,
    onSaveTitleClick: () -> Unit,
    onExportMarkdownClick: () -> Unit,
    onEditOutlineClick: (Long) -> Unit,
    onOpenPreviewClick: (Long) -> Unit,
    onSectionClick: (Long) -> Unit,
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
            value = uiState.title,
            onValueChange = onTitleChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleEditor.titleInput")
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
        // タイトル保存ボタン
        Button(
            onClick = onSaveTitleClick,
            enabled = !uiState.isSavingTitle,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleEditor.saveTitleButton")
                .semantics { contentDescription = "save_article_title" },
        ) {
            if (uiState.isSavingTitle) {
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
        ArticleCharacterCountSummary(uiState = uiState)

        // 目次
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.outline_heading),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedButton(
                onClick = {
                    val articleId = uiState.articleId
                    if (articleId != null) {
                        onEditOutlineClick(articleId)
                    }
                },
                enabled = uiState.articleId != null,
                modifier = Modifier
                    .testTag("articleEditor.editOutlineButton")
                    .semantics { contentDescription = "edit_outline" },
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                Text(
                    text = stringResource(R.string.edit_outline),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        if (uiState.sections.isEmpty()) {
            Text(
                text = stringResource(R.string.outline_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            uiState.sections.forEach { section ->
                ArticleEditorSectionCard(
                    section = section,
                    onClick = { onSectionClick(section.id) },
                )
            }
        }

        // ボタン
        OutlinedButton(
            onClick = {
                val articleId = uiState.articleId
                if (articleId != null) {
                    onOpenPreviewClick(articleId)
                }
            },
            enabled = uiState.articleId != null,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleEditor.openPreviewButton")
                .semantics { contentDescription = "open_article_preview" },
        ) {
            Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
            Text(
                text = stringResource(R.string.full_preview_heading),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Button(
            onClick = onExportMarkdownClick,
            enabled = !uiState.isExportingMarkdown,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("articleEditor.exportMarkdownButton")
                .semantics { contentDescription = "export_markdown" },
        ) {
            if (uiState.isExportingMarkdown) {
                CircularProgressIndicator()
            } else {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Text(
                    text = stringResource(R.string.export_markdown),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ArticleEditorSectionCard(
    section: ArticleEditorSectionUiState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("articleEditor.sectionCard.${section.id}"),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.outline_heading_format,
                    section.orderIndex + 1,
                    section.heading,
                ),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArticleApprovalChip(
                    userApproved = section.userApproved,
                    onClick = onClick,
                )
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.section_character_count_format,
                                section.characterCount,
                            ),
                        )
                    },
                    modifier = Modifier.testTag("articleEditor.sectionCharacterCount.${section.id}"),
                )
            }
        }
    }
}

@Composable
private fun ArticleCharacterCountSummary(uiState: ArticleEditorUiState) {
    Text(
        text = stringResource(
            R.string.article_total_character_count_format,
            uiState.totalCharacterCount,
        ),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag("articleEditor.totalCharacterCount"),
    )
}

@Composable
private fun ArticleApprovalChip(
    userApproved: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (userApproved) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = if (userApproved) {
                    stringResource(R.string.section_user_approved)
                } else {
                    stringResource(R.string.section_user_not_approved)
                },
            )
        },
        leadingIcon = if (userApproved) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            }
        } else {
            null
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = contentColor,
            leadingIconContentColor = contentColor,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = contentColor,
        ),
    )
}

@Composable
private fun MessageText(message: ArticleEditorMessage?) {
    val text = when (message) {
        null -> null
        ArticleEditorMessage.TitleSaved -> stringResource(R.string.article_title_saved)
        ArticleEditorMessage.TitleRequired -> stringResource(R.string.article_title_required)
        ArticleEditorMessage.SaveFailed -> stringResource(R.string.article_title_save_failed)
        ArticleEditorMessage.MarkdownExported -> stringResource(R.string.markdown_exported)
        ArticleEditorMessage.ExportTitleRequired -> stringResource(R.string.markdown_export_title_required)
        ArticleEditorMessage.ExportNotPhase2OrMissing -> stringResource(R.string.markdown_export_not_phase2)
        is ArticleEditorMessage.ExportUnapprovedSections -> stringResource(
            R.string.markdown_export_unapproved_sections,
            message.count,
        )

        ArticleEditorMessage.ExportWriteFailed -> stringResource(R.string.markdown_export_write_failed)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PreviewSections = listOf(
    ArticleEditorSectionUiState(
        id = 1,
        heading = "背景と解決したかったこと",
        orderIndex = 0,
        content = "Composeで状態を画面に閉じ込めすぎて、保存処理の見通しが悪くなった。",
        draftContent = "未保存の下書き",
        userApproved = true,
    ),
    ArticleEditorSectionUiState(
        id = 2,
        heading = "実装で詰まったポイント",
        orderIndex = 1,
        content = "",
        draftContent = "編集中の本文",
        userApproved = false,
    ),
)

@Preview(showBackground = true)
@Composable
private fun ArticleEditorReadyPreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(
                articleId = 1,
                title = "Compose Navigation を実装から理解する",
                sections = PreviewSections,
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorSavingMessagePreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(
                articleId = 1,
                title = "Compose Navigation を実装から理解する",
                sections = PreviewSections,
                message = ArticleEditorMessage.TitleSaved,
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorExportingPreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(
                articleId = 1,
                title = "Compose Navigation を実装から理解する",
                sections = PreviewSections,
                isExportingMarkdown = true,
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorExportBlockedPreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(
                articleId = 1,
                title = "Compose Navigation を実装から理解する",
                sections = PreviewSections,
                message = ArticleEditorMessage.ExportUnapprovedSections(1),
            ),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorLoadingPreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(isLoading = true),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorNotFoundPreview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(error = ArticleEditorError.NotFound),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleEditorNotPhase2Preview() {
    CreateBlogSupporterTheme {
        ArticleEditorScreen(
            uiState = ArticleEditorUiState(error = ArticleEditorError.NotPhase2),
            onBack = {},
            onTitleChanged = {},
            onSaveTitleClick = {},
            onExportMarkdownClick = {},
            onEditOutlineClick = {},
            onOpenPreviewClick = {},
            onSectionClick = {},
        )
    }
}
