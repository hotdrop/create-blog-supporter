package jp.hotdrop.createblogsupporter.ui.articleentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.hotdrop.createblogsupporter.R
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.ui.articleeditor.ArticleEditorRoute
import jp.hotdrop.createblogsupporter.ui.articlememo.ArticleMemoRoute

@Composable
fun ArticleEntryRoute(
    onBack: () -> Unit,
    onPhase1Saved: () -> Unit,
    onGenerateOutline: (Long) -> Unit,
    onEditOutline: (Long) -> Unit,
    onEditSection: (Long, Long) -> Unit,
    viewModel: ArticleEntryViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState.value) {
        ArticleEntryUiState.Loading -> ArticleEntryLoadingScreen(onBack = onBack)
        ArticleEntryUiState.NotFound -> ArticleEntryMissingScreen(onBack = onBack)
        is ArticleEntryUiState.Ready -> {
            when (state.phase) {
                ArticlePhase.Phase1 -> ArticleMemoRoute(
                    onBack = onBack,
                    onSaved = onPhase1Saved,
                    onGenerateOutline = onGenerateOutline,
                )

                ArticlePhase.Phase2 -> ArticleEditorRoute(
                    onBack = onBack,
                    onEditOutline = onEditOutline,
                    onEditSection = onEditSection,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleEntryLoadingScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { ArticleEntryTopAppBar(onBack = onBack) },
    ) { innerPadding ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleEntryMissingScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { ArticleEntryTopAppBar(onBack = onBack) },
    ) { innerPadding ->
        MissingContent(
            innerPadding = innerPadding,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleEntryTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.edit_article_title)) },
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
}

@Composable
private fun MissingContent(
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
