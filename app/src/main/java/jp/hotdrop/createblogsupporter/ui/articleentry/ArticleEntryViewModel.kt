package jp.hotdrop.createblogsupporter.ui.articleentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftHeaderUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArticleEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftHeaderUseCase: ObserveArticleDraftHeaderUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])

    val uiState: StateFlow<ArticleEntryUiState> =
        observeArticleDraftHeaderUseCase(articleId)
            .map { article ->
                if (article == null) {
                    ArticleEntryUiState.NotFound
                } else {
                    ArticleEntryUiState.Ready(article.phase)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArticleEntryUiState.Loading,
            )
}

sealed interface ArticleEntryUiState {
    data object Loading : ArticleEntryUiState
    data object NotFound : ArticleEntryUiState
    data class Ready(val phase: ArticlePhase) : ArticleEntryUiState
}
