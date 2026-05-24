package jp.hotdrop.createblogsupporter.ui.articlememo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.hotdrop.createblogsupporter.domain.usecase.CreatePhase1ArticleResult
import jp.hotdrop.createblogsupporter.domain.usecase.CreatePhase1ArticleUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase1ArticleResult
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase1ArticleUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleMemoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createPhase1ArticleUseCase: CreatePhase1ArticleUseCase,
    private val updatePhase1ArticleUseCase: UpdatePhase1ArticleUseCase,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
) : ViewModel() {
    private val articleId: Long? = savedStateHandle["articleId"]
    private var hasLoadedExistingArticle = false

    private val _uiState = MutableStateFlow(
        ArticleMemoUiState(
            mode = if (articleId == null) ArticleMemoMode.New else ArticleMemoMode.Edit,
            isLoading = articleId != null,
        ),
    )
    val uiState: StateFlow<ArticleMemoUiState> = _uiState.asStateFlow()

    private val _savedEvent = MutableSharedFlow<Unit>()
    val savedEvent: SharedFlow<Unit> = _savedEvent.asSharedFlow()

    private val _generateOutlineEvent = MutableSharedFlow<Long>()
    val generateOutlineEvent: SharedFlow<Long> = _generateOutlineEvent.asSharedFlow()

    init {
        if (articleId != null) {
            viewModelScope.launch {
                observeArticleDraftUseCase(articleId).collect { article ->
                    if (article == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ArticleMemoError.NotFound,
                            )
                        }
                    } else if (!hasLoadedExistingArticle) {
                        hasLoadedExistingArticle = true
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                topic = article.topic,
                                detail = article.detail,
                                error = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onTopicChanged(value: String) {
        _uiState.update {
            it.copy(topic = value, topicError = false, message = null)
        }
    }

    fun onDetailChanged(value: String) {
        _uiState.update { it.copy(detail = value, message = null) }
    }

    fun onGenerateOutlineClick() {
        val current = _uiState.value
        val editingArticleId = articleId
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, topicError = false, message = null) }
            val result = if (editingArticleId == null) {
                createPhase1ArticleUseCase(current.topic, current.detail)
            } else {
                updatePhase1ArticleUseCase(editingArticleId, current.topic, current.detail)
            }
            when (result) {
                is CreatePhase1ArticleResult.Created -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _generateOutlineEvent.emit(result.articleId)
                }

                CreatePhase1ArticleResult.InvalidTopic -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            topicError = true,
                            message = ArticleMemoMessage.TopicRequired,
                        )
                    }
                }

                UpdatePhase1ArticleResult.Updated -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _generateOutlineEvent.emit(checkNotNull(editingArticleId))
                }

                UpdatePhase1ArticleResult.InvalidTopic -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            topicError = true,
                            message = ArticleMemoMessage.TopicRequired,
                        )
                    }
                }

                UpdatePhase1ArticleResult.NotPhase1OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = ArticleMemoMessage.SaveFailed,
                        )
                    }
                }
            }
        }
    }

    fun onSaveClick() {
        val current = _uiState.value
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, topicError = false, message = null) }
            val result = if (articleId == null) {
                createPhase1ArticleUseCase(current.topic, current.detail)
            } else {
                updatePhase1ArticleUseCase(articleId, current.topic, current.detail)
            }
            when (result) {
                is CreatePhase1ArticleResult.Created -> {
                    _uiState.update { it.copy(isSaving = false, message = ArticleMemoMessage.Saved) }
                    _savedEvent.emit(Unit)
                }

                CreatePhase1ArticleResult.InvalidTopic -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            topicError = true,
                            message = ArticleMemoMessage.TopicRequired,
                        )
                    }
                }

                UpdatePhase1ArticleResult.Updated -> {
                    _uiState.update { it.copy(isSaving = false, message = ArticleMemoMessage.Saved) }
                    _savedEvent.emit(Unit)
                }

                UpdatePhase1ArticleResult.InvalidTopic -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            topicError = true,
                            message = ArticleMemoMessage.TopicRequired,
                        )
                    }
                }

                UpdatePhase1ArticleResult.NotPhase1OrMissing -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = ArticleMemoMessage.SaveFailed,
                        )
                    }
                }
            }
        }
    }
}

data class ArticleMemoUiState(
    val mode: ArticleMemoMode,
    val topic: String = "",
    val detail: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val topicError: Boolean = false,
    val message: ArticleMemoMessage? = null,
    val error: ArticleMemoError? = null,
)

enum class ArticleMemoMode {
    New,
    Edit,
}

enum class ArticleMemoMessage {
    TopicRequired,
    Saved,
    SaveFailed,
}

enum class ArticleMemoError {
    NotFound,
}
