package jp.hotdrop.createblogsupporter.ui.articleeditor

import android.content.ClipData
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import jp.hotdrop.createblogsupporter.R
import androidx.core.net.toUri

@Composable
fun ArticleEditorRoute(
    onBack: () -> Unit,
    onEditOutline: (Long) -> Unit,
    onEditSection: (Long, Long) -> Unit,
    viewModel: ArticleEditorViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.shareEvents.collectLatest { event ->
            when (event) {
                is ArticleEditorShareEvent.ShareMarkdown -> {
                    val uri = event.uriString.toUri()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, event.title)
                        clipData = ClipData.newUri(
                            context.contentResolver,
                            event.fileName,
                            uri,
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            context.getString(R.string.share_markdown),
                        ),
                    )
                }
            }
        }
    }

    ArticleEditorScreen(
        uiState = uiState.value,
        onBack = onBack,
        onTitleChanged = viewModel::onTitleChanged,
        onSaveTitleClick = viewModel::onSaveTitleClick,
        onExportMarkdownClick = viewModel::onExportMarkdownClick,
        onEditOutlineClick = { articleId ->
            onEditOutline(articleId)
        },
        onSectionClick = { sectionId ->
            uiState.value.articleId?.let { articleId ->
                onEditSection(articleId, sectionId)
            }
        },
    )
}
