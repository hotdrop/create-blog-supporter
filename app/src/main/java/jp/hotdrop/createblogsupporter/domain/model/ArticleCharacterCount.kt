package jp.hotdrop.createblogsupporter.domain.model

const val IdealArticleCharacterCount = 5000
const val MaxArticleCharacterCount = 7000

enum class ArticleCharacterCountStatus {
    UnderIdeal,
    IdealRange,
    OverLimit,
}

fun countEditableContentCharacters(
    content: String,
    draftContent: String,
): Int =
    countArticleCharacters(draftContent.ifBlank { content })

fun totalEditableContentCharacters(sections: List<ArticleSection>): Int =
    sections.sumOf { section ->
        countEditableContentCharacters(
            content = section.content,
            draftContent = section.draftContent,
        )
    }

fun articleCharacterCountStatus(totalCharacterCount: Int): ArticleCharacterCountStatus =
    when {
        totalCharacterCount > MaxArticleCharacterCount -> ArticleCharacterCountStatus.OverLimit
        totalCharacterCount >= IdealArticleCharacterCount -> ArticleCharacterCountStatus.IdealRange
        else -> ArticleCharacterCountStatus.UnderIdeal
    }

private fun countArticleCharacters(text: String): Int =
    text.codePoints()
        .filter { codePoint -> codePoint != '\n'.code && codePoint != '\r'.code }
        .count()
        .toInt()
