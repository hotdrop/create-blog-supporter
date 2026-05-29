package jp.hotdrop.createblogsupporter.data.llm

import java.io.File
import jp.hotdrop.createblogsupporter.domain.model.LlmSupportFailure
import jp.hotdrop.createblogsupporter.domain.usecase.LlmSupportException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiteRtLmBlogSupportClientTest {
    @Test
    fun resolveConfiguredLiteRtLmModelPath_returnsModelPathWhenFileExists() {
        val modelFile = File.createTempFile("blog-support", ".litertlm")
        try {
            assertEquals(
                modelFile.absolutePath,
                resolveConfiguredLiteRtLmModelPath(modelFile.absolutePath),
            )
        } finally {
            modelFile.delete()
        }
    }

    @Test
    fun resolveConfiguredLiteRtLmModelPath_returnsModelNotConfiguredWhenPathIsBlank() {
        val error = runCatching {
            resolveConfiguredLiteRtLmModelPath("")
        }.exceptionOrNull()

        assertTrue(error is LlmSupportException)
        assertEquals(LlmSupportFailure.ModelNotConfigured, (error as LlmSupportException).failure)
    }

    @Test
    fun resolveConfiguredLiteRtLmModelPath_returnsModelFileMissingWhenFileDoesNotExist() {
        val error = runCatching {
            resolveConfiguredLiteRtLmModelPath("/tmp/create-blog-supporter-missing.litertlm")
        }.exceptionOrNull()

        assertTrue(error is LlmSupportException)
        assertEquals(LlmSupportFailure.ModelFileMissing, (error as LlmSupportException).failure)
    }
}
