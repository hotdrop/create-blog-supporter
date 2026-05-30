package jp.hotdrop.createblogsupporter.ui.articlelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraftSummary
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftSummariesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArticleListViewModel @Inject constructor(
    observeArticleDraftSummariesUseCase: ObserveArticleDraftSummariesUseCase,
) : ViewModel() {
    val uiState: StateFlow<ArticleListUiState> =
        observeArticleDraftSummariesUseCase()
            .map { drafts ->
                ArticleListUiState.Ready(
                    articles = drafts.map { it.toArticleListItemUiState() },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArticleListUiState.Loading,
            )
}

sealed interface ArticleListUiState {
    data object Loading : ArticleListUiState
    data class Ready(val articles: List<ArticleListItemUiState>) : ArticleListUiState
}

data class ArticleListItemUiState(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
    val topic: String,
    val status: ArticleStatus,
    val updatedAt: Long,
    val allSectionsApproved: Boolean,
)

private fun ArticleDraftSummary.toArticleListItemUiState(): ArticleListItemUiState =
    ArticleListItemUiState(
        id = id,
        phase = phase,
        title = title,
        topic = topic,
        status = status,
        updatedAt = updatedAt,
        allSectionsApproved = allSectionsApproved,
    )
