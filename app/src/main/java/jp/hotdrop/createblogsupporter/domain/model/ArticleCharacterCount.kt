package jp.hotdrop.createblogsupporter.domain.model

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

private fun countArticleCharacters(text: String): Int =
    text.codePoints()
        .filter { codePoint -> codePoint != '\n'.code && codePoint != '\r'.code }
        .count()
        .toInt()
