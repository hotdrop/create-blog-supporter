package jp.hotdrop.createblogsupporter.ui.outlineedit

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
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
fun OutlineEditScreen(
    uiState: OutlineEditUiState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEditHeadingClick: (OutlineEditSectionUiState) -> Unit,
    onDeleteClick: (OutlineEditSectionUiState) -> Unit,
    onDialogHeadingChanged: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAdd: () -> Unit,
    onConfirmEditHeading: () -> Unit,
    onConfirmDelete: () -> Unit,
    onMoveUpClick: (Long) -> Unit,
    onMoveDownClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.outline_edit_title)) },
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

            else -> OutlineEditContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onAddClick = onAddClick,
                onEditHeadingClick = onEditHeadingClick,
                onDeleteClick = onDeleteClick,
                onMoveUpClick = onMoveUpClick,
                onMoveDownClick = onMoveDownClick,
            )
        }
    }
    OutlineEditDialogContent(
        dialog = uiState.dialog,
        isOperating = uiState.isOperating,
        onHeadingChanged = onDialogHeadingChanged,
        onDismissDialog = onDismissDialog,
        onConfirmAdd = onConfirmAdd,
        onConfirmEditHeading = onConfirmEditHeading,
        onConfirmDelete = onConfirmDelete,
    )
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
    error: OutlineEditError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        OutlineEditError.NotFound -> stringResource(R.string.unknown_article)
        OutlineEditError.NotPhase2 -> stringResource(R.string.article_editor_not_phase2)
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
private fun OutlineEditContent(
    uiState: OutlineEditUiState,
    innerPadding: PaddingValues,
    onAddClick: () -> Unit,
    onEditHeadingClick: (OutlineEditSectionUiState) -> Unit,
    onDeleteClick: (OutlineEditSectionUiState) -> Unit,
    onMoveUpClick: (Long) -> Unit,
    onMoveDownClick: (Long) -> Unit,
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
            text = uiState.title,
            style = MaterialTheme.typography.titleMedium,
        )
        Button(
            onClick = onAddClick,
            enabled = !uiState.isOperating,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("outlineEdit.addButton")
                .semantics { contentDescription = "add_outline_section" },
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.add_section),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        MessageText(message = uiState.message)
        if (uiState.sections.isEmpty()) {
            Text(
                text = stringResource(R.string.outline_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            uiState.sections.forEachIndexed { index, section ->
                OutlineEditSectionCard(
                    section = section,
                    isFirst = index == 0,
                    isLast = index == uiState.sections.lastIndex,
                    isOperating = uiState.isOperating,
                    onEditHeadingClick = onEditHeadingClick,
                    onDeleteClick = onDeleteClick,
                    onMoveUpClick = onMoveUpClick,
                    onMoveDownClick = onMoveDownClick,
                )
            }
        }
    }
}

@Composable
private fun OutlineEditSectionCard(
    section: OutlineEditSectionUiState,
    isFirst: Boolean,
    isLast: Boolean,
    isOperating: Boolean,
    onEditHeadingClick: (OutlineEditSectionUiState) -> Unit,
    onDeleteClick: (OutlineEditSectionUiState) -> Unit,
    onMoveUpClick: (Long) -> Unit,
    onMoveDownClick: (Long) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (section.hasContent) {
                                stringResource(R.string.section_content_saved)
                            } else {
                                stringResource(R.string.section_content_empty)
                            },
                        )
                    },
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (section.userApproved) {
                                stringResource(R.string.section_user_approved)
                            } else {
                                stringResource(R.string.section_user_not_approved)
                            },
                        )
                    },
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onMoveUpClick(section.id) },
                        enabled = !isOperating && !isFirst,
                        modifier = Modifier
                            .testTag("outlineEdit.moveUpButton")
                            .semantics { contentDescription = "move_section_up" },
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.move_section_up),
                        )
                    }
                    IconButton(
                        onClick = { onMoveDownClick(section.id) },
                        enabled = !isOperating && !isLast,
                        modifier = Modifier
                            .testTag("outlineEdit.moveDownButton")
                            .semantics { contentDescription = "move_section_down" },
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.move_section_down),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onEditHeadingClick(section) },
                        enabled = !isOperating,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_section_heading),
                        )
                    }
                    IconButton(
                        onClick = { onDeleteClick(section) },
                        enabled = !isOperating,
                        modifier = Modifier
                            .testTag("outlineEdit.deleteButton")
                            .semantics { contentDescription = "delete_section" },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_section),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineEditDialogContent(
    dialog: OutlineEditDialog?,
    isOperating: Boolean,
    onHeadingChanged: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmAdd: () -> Unit,
    onConfirmEditHeading: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    when (dialog) {
        is OutlineEditDialog.Add -> HeadingDialog(
            title = stringResource(R.string.add_section),
            heading = dialog.heading,
            headingError = dialog.headingError,
            confirmText = stringResource(R.string.add),
            isOperating = isOperating,
            onHeadingChanged = onHeadingChanged,
            onDismissDialog = onDismissDialog,
            onConfirm = onConfirmAdd,
        )

        is OutlineEditDialog.EditHeading -> HeadingDialog(
            title = stringResource(R.string.edit_section_heading),
            heading = dialog.heading,
            headingError = dialog.headingError,
            confirmText = stringResource(R.string.save),
            isOperating = isOperating,
            onHeadingChanged = onHeadingChanged,
            onDismissDialog = onDismissDialog,
            onConfirm = onConfirmEditHeading,
        )

        is OutlineEditDialog.ConfirmDelete -> AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(text = stringResource(R.string.delete_section)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.delete_section_confirm,
                        dialog.heading,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    enabled = !isOperating,
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissDialog,
                    enabled = !isOperating,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )

        null -> Unit
    }
}

@Composable
private fun HeadingDialog(
    title: String,
    heading: String,
    headingError: Boolean,
    confirmText: String,
    isOperating: Boolean,
    onHeadingChanged: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissDialog,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = heading,
                onValueChange = onHeadingChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("outlineEdit.headingInput")
                    .semantics { contentDescription = "outline_section_heading_input" },
                label = { Text(text = stringResource(R.string.section_heading_label)) },
                isError = headingError,
                supportingText = {
                    if (headingError) {
                        Text(text = stringResource(R.string.section_heading_required))
                    }
                },
                singleLine = false,
                minLines = 1,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isOperating,
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissDialog,
                enabled = !isOperating,
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun MessageText(message: OutlineEditMessage?) {
    val text = when (message) {
        null -> null
        OutlineEditMessage.SectionAdded -> stringResource(R.string.section_added)
        OutlineEditMessage.SectionUpdated -> stringResource(R.string.section_updated)
        OutlineEditMessage.SectionDeleted -> stringResource(R.string.section_deleted)
        OutlineEditMessage.SectionMoved -> stringResource(R.string.section_moved)
        OutlineEditMessage.HeadingRequired -> stringResource(R.string.section_heading_required)
        OutlineEditMessage.LastSectionCannotDelete -> stringResource(R.string.last_section_cannot_delete)
        OutlineEditMessage.OperationFailed -> stringResource(R.string.outline_operation_failed)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PreviewOutlineSections = listOf(
    OutlineEditSectionUiState(
        id = 1,
        heading = "背景と解決したかったこと",
        orderIndex = 0,
        hasContent = true,
        userApproved = true,
    ),
    OutlineEditSectionUiState(
        id = 2,
        heading = "実装で詰まったポイント",
        orderIndex = 1,
        hasContent = false,
        userApproved = false,
    ),
)

@Preview(showBackground = true)
@Composable
private fun OutlineEditReadyPreview() {
    CreateBlogSupporterTheme {
        OutlineEditScreen(
            uiState = OutlineEditUiState(
                title = "Compose Navigation を実装から理解する",
                sections = PreviewOutlineSections,
            ),
            onBack = {},
            onAddClick = {},
            onEditHeadingClick = {},
            onDeleteClick = {},
            onDialogHeadingChanged = {},
            onDismissDialog = {},
            onConfirmAdd = {},
            onConfirmEditHeading = {},
            onConfirmDelete = {},
            onMoveUpClick = {},
            onMoveDownClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineEditAddDialogPreview() {
    CreateBlogSupporterTheme {
        OutlineEditScreen(
            uiState = OutlineEditUiState(
                title = "Compose Navigation を実装から理解する",
                sections = PreviewOutlineSections,
                dialog = OutlineEditDialog.Add(heading = "", headingError = true),
                message = OutlineEditMessage.HeadingRequired,
            ),
            onBack = {},
            onAddClick = {},
            onEditHeadingClick = {},
            onDeleteClick = {},
            onDialogHeadingChanged = {},
            onDismissDialog = {},
            onConfirmAdd = {},
            onConfirmEditHeading = {},
            onConfirmDelete = {},
            onMoveUpClick = {},
            onMoveDownClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineEditDeleteDialogPreview() {
    CreateBlogSupporterTheme {
        OutlineEditScreen(
            uiState = OutlineEditUiState(
                title = "Compose Navigation を実装から理解する",
                sections = PreviewOutlineSections,
                dialog = OutlineEditDialog.ConfirmDelete(
                    sectionId = 1,
                    heading = "背景と解決したかったこと",
                ),
            ),
            onBack = {},
            onAddClick = {},
            onEditHeadingClick = {},
            onDeleteClick = {},
            onDialogHeadingChanged = {},
            onDismissDialog = {},
            onConfirmAdd = {},
            onConfirmEditHeading = {},
            onConfirmDelete = {},
            onMoveUpClick = {},
            onMoveDownClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineEditLoadingPreview() {
    CreateBlogSupporterTheme {
        OutlineEditScreen(
            uiState = OutlineEditUiState(isLoading = true),
            onBack = {},
            onAddClick = {},
            onEditHeadingClick = {},
            onDeleteClick = {},
            onDialogHeadingChanged = {},
            onDismissDialog = {},
            onConfirmAdd = {},
            onConfirmEditHeading = {},
            onConfirmDelete = {},
            onMoveUpClick = {},
            onMoveDownClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineEditErrorPreview() {
    CreateBlogSupporterTheme {
        OutlineEditScreen(
            uiState = OutlineEditUiState(error = OutlineEditError.NotPhase2),
            onBack = {},
            onAddClick = {},
            onEditHeadingClick = {},
            onDeleteClick = {},
            onDialogHeadingChanged = {},
            onDismissDialog = {},
            onConfirmAdd = {},
            onConfirmEditHeading = {},
            onConfirmDelete = {},
            onMoveUpClick = {},
            onMoveDownClick = {},
        )
    }
}
