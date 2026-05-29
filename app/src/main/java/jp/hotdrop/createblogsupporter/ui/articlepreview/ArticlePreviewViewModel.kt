package jp.hotdrop.createblogsupporter.ui.articlepreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateMarkdownUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ArticlePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
    observeArticleSectionsUseCase: ObserveArticleSectionsUseCase,
    private val generateMarkdownUseCase: GenerateMarkdownUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])

    private val _uiState = MutableStateFlow(ArticlePreviewUiState(isLoading = true))
    val uiState: StateFlow<ArticlePreviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeArticleDraftUseCase(articleId),
                observeArticleSectionsUseCase(articleId),
            ) { article, sections ->
                article to sections
            }.collect { (article, sections) ->
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticlePreviewError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticlePreviewError.NotPhase2,
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                title = article.title,
                                previewText = generateMarkdownUseCase(
                                    title = article.title,
                                    sections = sections,
                                ).trimEnd(),
                                hasSavedSectionContent = sections.any { section -> section.content.isNotBlank() },
                                error = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ArticlePreviewUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val previewText: String = "",
    val hasSavedSectionContent: Boolean = false,
    val error: ArticlePreviewError? = null,
)

enum class ArticlePreviewError {
    NotFound,
    NotPhase2,
}
