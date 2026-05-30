package jp.hotdrop.createblogsupporter.ui.articlelist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.hotdrop.createblogsupporter.R
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.ui.theme.CreateBlogSupporterTheme
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListScreen(
    uiState: ArticleListUiState,
    onCreateArticle: () -> Unit,
    onOpenArticle: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.article_list_title)) },
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .testTag("articleList.settingsButton")
                            .semantics { contentDescription = "open_llm_settings" },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.llm_settings_title),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateArticle,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(text = stringResource(R.string.create_article)) },
                modifier = Modifier.semantics {
                    contentDescription = "create_article"
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ArticleListUiState.Loading -> {
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

            is ArticleListUiState.Ready -> {
                if (uiState.articles.isEmpty()) {
                    EmptyArticleList(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.articles,
                            key = { it.id },
                        ) { article ->
                            ArticleListCard(
                                article = article,
                                onClick = { onOpenArticle(article.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyArticleList(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.article_list_empty_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.article_list_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ArticleListCard(
    article: ArticleListItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ArticlePhaseChip(
                    article = article,
                    onClick = onClick,
                )
                Text(
                    text = if (article.isComplete()) {
                        stringResource(R.string.status_all_sections_approved)
                    } else {
                        stringResource(R.string.status_draft)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = article.title.ifBlank { article.topic },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    R.string.updated_at_format,
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(Date(article.updatedAt)),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ArticlePhaseChip(
    article: ArticleListItemUiState,
    onClick: () -> Unit,
) {
    val chipColors = article.phaseChipColors()
    AssistChip(
        onClick = onClick,
        label = {
            Text(text = article.phaseLabel())
        },
        leadingIcon = if (article.isComplete()) {
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
            containerColor = chipColors.container,
            labelColor = chipColors.content,
            leadingIconContentColor = chipColors.content,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = chipColors.border,
        ),
    )
}

@Composable
private fun ArticleListItemUiState.phaseLabel(): String =
    if (isComplete()) {
        stringResource(R.string.article_complete_label)
    } else {
        when (phase) {
            ArticlePhase.Phase1 -> stringResource(R.string.phase1_label)
            ArticlePhase.Phase2 -> stringResource(R.string.phase2_label)
        }
    }

private fun ArticleListItemUiState.isComplete(): Boolean =
    phase == ArticlePhase.Phase2 && allSectionsApproved

@Composable
private fun ArticleListItemUiState.phaseChipColors(): PhaseChipColors =
    if (isComplete()) {
        PhaseChipColors(
            container = CompleteChipContainer,
            content = CompleteChipContent,
            border = CompleteChipBorder,
        )
    } else {
        when (phase) {
            ArticlePhase.Phase1 -> PhaseChipColors(
                container = MaterialTheme.colorScheme.secondaryContainer,
                content = MaterialTheme.colorScheme.onSecondaryContainer,
                border = MaterialTheme.colorScheme.secondary,
            )

            ArticlePhase.Phase2 -> PhaseChipColors(
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                border = MaterialTheme.colorScheme.tertiary,
            )
        }
    }

private data class PhaseChipColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

private val CompleteChipContainer = Color(0xFF0F4D2F)
private val CompleteChipContent = Color(0xFFA7F3C1)
private val CompleteChipBorder = Color(0xFF4ADE80)

@Preview(showBackground = true)
@Composable
private fun ArticleListLoadingPreview() {
    CreateBlogSupporterTheme {
        ArticleListScreen(
            uiState = ArticleListUiState.Loading,
            onCreateArticle = {},
            onOpenArticle = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListEmptyPreview() {
    CreateBlogSupporterTheme {
        ArticleListScreen(
            uiState = ArticleListUiState.Ready(emptyList()),
            onCreateArticle = {},
            onOpenArticle = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleListReadyPreview() {
    CreateBlogSupporterTheme {
        ArticleListScreen(
            uiState = ArticleListUiState.Ready(
                articles = listOf(
                    ArticleListItemUiState(
                        id = 1,
                        phase = ArticlePhase.Phase1,
                        title = "",
                        topic = "Composeの状態管理について書く",
                        status = ArticleStatus.Draft,
                        updatedAt = 1_700_000_000_000,
                        allSectionsApproved = false,
                    ),
                    ArticleListItemUiState(
                        id = 2,
                        phase = ArticlePhase.Phase2,
                        title = "RoomのFlowを使った一覧更新",
                        topic = "RoomとFlow",
                        status = ArticleStatus.Draft,
                        updatedAt = 1_700_100_000_000,
                        allSectionsApproved = false,
                    ),
                    ArticleListItemUiState(
                        id = 3,
                        phase = ArticlePhase.Phase2,
                        title = "Compose Previewの整え方",
                        topic = "Compose Preview",
                        status = ArticleStatus.Draft,
                        updatedAt = 1_700_200_000_000,
                        allSectionsApproved = true,
                    ),
                ),
            ),
            onCreateArticle = {},
            onOpenArticle = {},
            onOpenSettings = {},
        )
    }
}
