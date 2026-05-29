package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticleCharacterCountStatus
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.model.articleCharacterCountStatus
import jp.hotdrop.createblogsupporter.domain.model.countEditableContentCharacters
import jp.hotdrop.createblogsupporter.domain.model.totalEditableContentCharacters
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleCharacterCountTest {
    @Test
    fun countEditableContentCharacters_prefersDraftContent() {
        val count = countEditableContentCharacters(
            content = "保存済み本文",
            draftContent = "編集中本文",
        )

        assertEquals(5, count)
    }

    @Test
    fun countEditableContentCharacters_usesContentWhenDraftIsBlank() {
        val count = countEditableContentCharacters(
            content = "保存済み本文",
            draftContent = "",
        )

        assertEquals(6, count)
    }

    @Test
    fun countEditableContentCharacters_excludesLineBreaksAndCountsUnicodeCodePoints() {
        val count = countEditableContentCharacters(
            content = "",
            draftContent = "a\nb\r\n🙂",
        )

        assertEquals(3, count)
    }

    @Test
    fun totalEditableContentCharacters_sumsSectionsWithDraftPriority() {
        val total = totalEditableContentCharacters(
            listOf(
                section(content = "保存済み", draftContent = "下書き"),
                section(content = "保存済み本文", draftContent = ""),
            ),
        )

        assertEquals(9, total)
    }

    @Test
    fun articleCharacterCountStatus_classifiesUnderIdealIdealRangeAndOverLimit() {
        assertEquals(ArticleCharacterCountStatus.UnderIdeal, articleCharacterCountStatus(4999))
        assertEquals(ArticleCharacterCountStatus.IdealRange, articleCharacterCountStatus(5000))
        assertEquals(ArticleCharacterCountStatus.IdealRange, articleCharacterCountStatus(7000))
        assertEquals(ArticleCharacterCountStatus.OverLimit, articleCharacterCountStatus(7001))
    }

    private fun section(
        content: String,
        draftContent: String,
    ): ArticleSection =
        ArticleSection(
            id = 1,
            articleId = 1,
            heading = "見出し",
            orderIndex = 0,
            content = content,
            draftContent = draftContent,
            proofreadStatus = ProofreadStatus.Unchecked,
            proofreadMessage = null,
            userApproved = false,
            createdAt = 1,
            updatedAt = 1,
            lastSavedAt = null,
            draftUpdatedAt = null,
        )
}
