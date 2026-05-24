package jp.hotdrop.createblogsupporter.ui.outlineproposal

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import jp.hotdrop.createblogsupporter.domain.model.OutlineProposal
import jp.hotdrop.createblogsupporter.domain.model.TitleProposal
import jp.hotdrop.createblogsupporter.ui.theme.CreateBlogSupporterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlineProposalScreen(
    uiState: OutlineProposalUiState,
    onBack: () -> Unit,
    onTitleSelected: (String) -> Unit,
    onOutlineSelected: (String) -> Unit,
    onAdoptClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.outline_proposal_title)) },
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

            else -> ProposalContent(
                uiState = uiState,
                innerPadding = innerPadding,
                onTitleSelected = onTitleSelected,
                onOutlineSelected = onOutlineSelected,
                onAdoptClick = onAdoptClick,
                onBack = onBack,
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
    error: OutlineProposalError,
    onBack: () -> Unit,
) {
    val message = when (error) {
        OutlineProposalError.NotFound -> stringResource(R.string.unknown_article)
        OutlineProposalError.NotPhase1 -> stringResource(R.string.outline_proposal_not_phase1)
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
private fun ProposalContent(
    uiState: OutlineProposalUiState,
    innerPadding: PaddingValues,
    onTitleSelected: (String) -> Unit,
    onOutlineSelected: (String) -> Unit,
    onAdoptClick: () -> Unit,
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
        Text(
            text = stringResource(R.string.title_proposals_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        uiState.titleProposals.forEach { proposal ->
            TitleProposalCard(
                proposal = proposal,
                selected = proposal.id == uiState.selectedTitleId,
                onClick = { onTitleSelected(proposal.id) },
            )
        }
        Text(
            text = stringResource(R.string.outline_proposals_heading),
            style = MaterialTheme.typography.titleMedium,
        )
        uiState.outlineProposals.forEach { proposal ->
            OutlineProposalCard(
                proposal = proposal,
                selected = proposal.id == uiState.selectedOutlineId,
                onClick = { onOutlineSelected(proposal.id) },
            )
        }
        MessageText(message = uiState.message)
        Button(
            onClick = onAdoptClick,
            enabled = !uiState.isAdopting,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "adopt_outline_proposal" },
        ) {
            if (uiState.isAdopting) {
                CircularProgressIndicator()
            } else {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Text(
                    text = stringResource(R.string.adopt_selected_proposal),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        OutlinedButton(
            onClick = onBack,
            enabled = !uiState.isAdopting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.cancel))
        }
    }
}

@Composable
private fun TitleProposalCard(
    proposal: TitleProposal,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
            )
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun OutlineProposalCard(
    proposal: OutlineProposal,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = proposal.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                proposal.headings.forEachIndexed { index, heading ->
                    Text(
                        text = stringResource(R.string.outline_heading_format, index + 1, heading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageText(message: OutlineProposalMessage?) {
    val text = when (message) {
        null -> null
        OutlineProposalMessage.SelectProposal -> stringResource(R.string.select_outline_proposal)
        OutlineProposalMessage.AdoptFailed -> stringResource(R.string.adopt_outline_failed)
        OutlineProposalMessage.GenerationFailed -> stringResource(R.string.llm_support_generation_failed)
    }
    if (text != null) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val PreviewTitles = listOf(
    TitleProposal("title-1", "Compose Navigation を実装から理解する"),
    TitleProposal("title-2", "Compose Navigation の設計判断を振り返る"),
    TitleProposal("title-3", "Compose Navigation でつまずいた点と改善策"),
)

private val PreviewOutlines = listOf(
    OutlineProposal(
        id = "outline-1",
        name = "実装手順から整理する構成",
        headings = listOf("背景", "設計方針", "実装の流れ", "改善点"),
    ),
    OutlineProposal(
        id = "outline-2",
        name = "読者の疑問に答える構成",
        headings = listOf("なぜ扱うのか", "前提知識", "具体例", "注意点"),
    ),
)

@Preview(showBackground = true)
@Composable
private fun OutlineProposalReadyPreview() {
    CreateBlogSupporterTheme {
        OutlineProposalScreen(
            uiState = OutlineProposalUiState(
                titleProposals = PreviewTitles,
                outlineProposals = PreviewOutlines,
                selectedTitleId = "title-1",
                selectedOutlineId = "outline-1",
            ),
            onBack = {},
            onTitleSelected = {},
            onOutlineSelected = {},
            onAdoptClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineProposalAdoptingPreview() {
    CreateBlogSupporterTheme {
        OutlineProposalScreen(
            uiState = OutlineProposalUiState(
                isAdopting = true,
                titleProposals = PreviewTitles,
                outlineProposals = PreviewOutlines,
                selectedTitleId = "title-1",
                selectedOutlineId = "outline-1",
            ),
            onBack = {},
            onTitleSelected = {},
            onOutlineSelected = {},
            onAdoptClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineProposalLoadingPreview() {
    CreateBlogSupporterTheme {
        OutlineProposalScreen(
            uiState = OutlineProposalUiState(isLoading = true),
            onBack = {},
            onTitleSelected = {},
            onOutlineSelected = {},
            onAdoptClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineProposalErrorPreview() {
    CreateBlogSupporterTheme {
        OutlineProposalScreen(
            uiState = OutlineProposalUiState(error = OutlineProposalError.NotPhase1),
            onBack = {},
            onTitleSelected = {},
            onOutlineSelected = {},
            onAdoptClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlineProposalEmptyPreview() {
    CreateBlogSupporterTheme {
        OutlineProposalScreen(
            uiState = OutlineProposalUiState(message = OutlineProposalMessage.SelectProposal),
            onBack = {},
            onTitleSelected = {},
            onOutlineSelected = {},
            onAdoptClick = {},
        )
    }
}
