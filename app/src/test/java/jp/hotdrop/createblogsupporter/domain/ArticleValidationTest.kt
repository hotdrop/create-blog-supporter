package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.validation.ArticleValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleValidationTest {
    @Test
    fun topic_isRequired_afterTrimming() {
        assertFalse(ArticleValidation.isValidTopic(""))
        assertFalse(ArticleValidation.isValidTopic("   "))
        assertTrue(ArticleValidation.isValidTopic("Compose UI"))
    }

    @Test
    fun topicAndDetail_areNormalizedByTrimming() {
        assertEquals("Compose UI", ArticleValidation.normalizeTopic("  Compose UI  "))
        assertEquals("details", ArticleValidation.normalizeDetail("\n details \n"))
    }

    @Test
    fun enumStorageValues_areStable() {
        assertEquals("phase1", ArticlePhase.Phase1.storageValue)
        assertEquals("phase2", ArticlePhase.Phase2.storageValue)
        assertEquals("draft", ArticleStatus.Draft.storageValue)
        assertEquals("readyToExport", ArticleStatus.ReadyToExport.storageValue)
        assertEquals("exported", ArticleStatus.Exported.storageValue)
        assertEquals("unchecked", ProofreadStatus.Unchecked.storageValue)
        assertEquals("checking", ProofreadStatus.Checking.storageValue)
        assertEquals("checked", ProofreadStatus.Checked.storageValue)
        assertEquals("error", ProofreadStatus.Error.storageValue)
    }
}
