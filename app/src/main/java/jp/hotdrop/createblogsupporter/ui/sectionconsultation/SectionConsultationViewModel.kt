package jp.hotdrop.createblogsupporter.ui.sectionconsultation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationRequest
import jp.hotdrop.createblogsupporter.domain.model.SectionConsultationSectionContext
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateSectionPastePromptUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleDraftUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.ObserveArticleSectionsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SectionConsultationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeArticleDraftUseCase: ObserveArticleDraftUseCase,
    observeArticleSectionsUseCase: ObserveArticleSectionsUseCase,
    private val generateSectionPastePromptUseCase: GenerateSectionPastePromptUseCase,
) : ViewModel() {
    private val articleId: Long = checkNotNull(savedStateHandle["articleId"])
    private val sectionId: Long = checkNotNull(savedStateHandle["sectionId"])

    private val _uiState = MutableStateFlow(SectionConsultationUiState(isLoading = true))
    val uiState: StateFlow<SectionConsultationUiState> = _uiState.asStateFlow()

    private val _copyEvents = MutableSharedFlow<String>()
    val copyEvents: SharedFlow<String> = _copyEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                observeArticleDraftUseCase(articleId),
                observeArticleSectionsUseCase(articleId),
            ) { article, sections ->
                article to sections
            }.collect { (article, sections) ->
                val section = sections.firstOrNull { it.id == sectionId }
                when {
                    article == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionConsultationError.NotFound,
                            )
                        }
                    }

                    article.phase != ArticlePhase.Phase2 -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionConsultationError.NotPhase2,
                            )
                        }
                    }

                    section == null -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = SectionConsultationError.NotFound,
                            )
                        }
                    }

                    else -> applyArticleAndSection(article, section, sections)
                }
            }
        }
    }

    fun onConsultationInputChanged(value: String) {
        _uiState.update {
            it.copy(
                consultationInput = value,
                message = null,
            )
        }
    }

    fun onCreatePastePromptClick() {
        val current = _uiState.value
        val targetSection = SectionConsultationSectionContext(
            orderIndex = current.orderIndex,
            heading = current.heading,
            savedContent = current.content,
            draftContent = current.draftContent,
            isTarget = true,
        )
        val outlineContext = current.outlineContext
            .filterNot { it.orderIndex == current.orderIndex || it.isTarget }
            .plus(targetSection)
            .sortedBy { it.orderIndex }
        val pastePrompt = generateSectionPastePromptUseCase(
            SectionConsultationRequest(
                articleTitle = current.articleTitle,
                topic = current.topic,
                detail = current.detail,
                targetSection = targetSection,
                outlineContext = outlineContext,
                userQuestion = current.consultationInput.trim(),
            ),
        )
        _uiState.update {
            it.copy(
                consultationAnswer = pastePrompt,
                message = null,
            )
        }
    }

    fun onCopyConsultationAnswerClick() {
        val answer = _uiState.value.consultationAnswer
        if (answer.isBlank()) return
        viewModelScope.launch {
            _copyEvents.emit(answer)
            _uiState.update { it.copy(message = SectionConsultationMessage.Copied) }
        }
    }

    private fun applyArticleAndSection(
        article: ArticleDraft,
        section: ArticleSection,
        sections: List<ArticleSection>,
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                error = null,
                articleTitle = article.title,
                topic = article.topic,
                detail = article.detail,
                heading = section.heading,
                orderIndex = section.orderIndex,
                content = section.content,
                draftContent = section.draftContent,
                outlineContext = sections.map { sectionContext ->
                    sectionContext.toConsultationContext(isTarget = sectionContext.id == sectionId)
                },
            )
        }
    }
}

data class SectionConsultationUiState(
    val articleTitle: String = "",
    val topic: String = "",
    val detail: String = "",
    val heading: String = "",
    val orderIndex: Int = 0,
    val content: String = "",
    val draftContent: String = "",
    val outlineContext: List<SectionConsultationSectionContext> = emptyList(),
    val consultationInput: String = "",
    val consultationAnswer: String = "",
    val message: SectionConsultationMessage? = null,
    val isLoading: Boolean = false,
    val error: SectionConsultationError? = null,
)

enum class SectionConsultationMessage {
    Copied,
}

enum class SectionConsultationError {
    NotFound,
    NotPhase2,
}

private fun ArticleSection.toConsultationContext(isTarget: Boolean): SectionConsultationSectionContext =
    SectionConsultationSectionContext(
        orderIndex = orderIndex,
        heading = heading,
        savedContent = content,
        draftContent = draftContent,
        isTarget = isTarget,
    )
