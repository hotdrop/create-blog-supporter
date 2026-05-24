package jp.hotdrop.createblogsupporter.domain.usecase

import jp.hotdrop.createblogsupporter.data.export.MarkdownExportFile
import jp.hotdrop.createblogsupporter.data.export.MarkdownFileWriter
import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class GenerateMarkdownUseCase @Inject constructor() {
    operator fun invoke(
        title: String,
        sections: List<ArticleSection>,
    ): String =
        buildString {
            append("# ")
            append(title.trim())
            sections.sortedBy { it.orderIndex }.forEach { section ->
                append("\n\n## ")
                append(section.heading.trim())
                if (section.content.isNotBlank()) {
                    append("\n\n")
                    append(section.content)
                }
            }
            append('\n')
        }
}

class ExportMarkdownUseCase @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val generateMarkdownUseCase: GenerateMarkdownUseCase,
    private val markdownFileWriter: MarkdownFileWriter,
) {
    suspend operator fun invoke(articleId: Long): ExportMarkdownResult {
        val article = articleRepository.getArticleDraft(articleId)
            ?: return ExportMarkdownResult.NotPhase2OrMissing
        if (article.phase != ArticlePhase.Phase2) {
            return ExportMarkdownResult.NotPhase2OrMissing
        }
        if (article.title.isBlank()) {
            return ExportMarkdownResult.BlankTitle
        }

        val sections = articleRepository.getArticleSections(articleId)
        val unapprovedCount = sections.count { !it.userApproved }
        if (unapprovedCount > 0) {
            return ExportMarkdownResult.UnapprovedSections(unapprovedCount)
        }

        val nowMillis = System.currentTimeMillis()
        val markdown = generateMarkdownUseCase(
            title = article.title,
            sections = sections,
        )
        val exportFile = try {
            markdownFileWriter.writeMarkdown(
                title = article.title,
                markdown = markdown,
                nowMillis = nowMillis,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return ExportMarkdownResult.WriteFailed
        }

        val markedExported = articleRepository.markArticleExported(
            articleId = article.id,
            nowMillis = nowMillis,
        )
        if (!markedExported) {
            return ExportMarkdownResult.NotPhase2OrMissing
        }
        return ExportMarkdownResult.Exported(
            file = exportFile,
            title = article.title.trim(),
        )
    }
}

sealed interface ExportMarkdownResult {
    data class Exported(
        val file: MarkdownExportFile,
        val title: String,
    ) : ExportMarkdownResult

    data object BlankTitle : ExportMarkdownResult
    data object NotPhase2OrMissing : ExportMarkdownResult
    data class UnapprovedSections(val count: Int) : ExportMarkdownResult
    data object WriteFailed : ExportMarkdownResult
}
