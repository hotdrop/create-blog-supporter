package jp.hotdrop.createblogsupporter.data

import jp.hotdrop.createblogsupporter.data.repository.isLiteRtLmModelFileName
import jp.hotdrop.createblogsupporter.data.repository.sanitizeModelFileName
import jp.hotdrop.createblogsupporter.domain.model.LlmSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmSettingsRepositoryTest {
    @Test
    fun isLiteRtLmModelFileName_acceptsLitertlmExtensionIgnoringCase() {
        assertTrue(isLiteRtLmModelFileName("blog-support.litertlm"))
        assertTrue(isLiteRtLmModelFileName("blog-support.LITERTLM"))
    }

    @Test
    fun isLiteRtLmModelFileName_rejectsOtherExtensions() {
        assertFalse(isLiteRtLmModelFileName("blog-support.bin"))
        assertFalse(isLiteRtLmModelFileName("blog-support.litertlm.tmp"))
    }

    @Test
    fun sanitizeModelFileName_replacesUnsafeCharacters() {
        assertEquals(
            "gemma_blog_support_.litertlm",
            sanitizeModelFileName("gemma blog/support?.litertlm"),
        )
    }

    @Test
    fun defaultSettings_hasNoConfiguredModel() {
        assertFalse(LlmSettings().isModelConfigured)
    }
}
