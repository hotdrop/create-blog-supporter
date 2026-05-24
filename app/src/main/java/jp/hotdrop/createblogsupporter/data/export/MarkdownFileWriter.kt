package jp.hotdrop.createblogsupporter.data.export

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class MarkdownExportFile(
    val uriString: String,
    val fileName: String,
)

interface MarkdownFileWriter {
    fun writeMarkdown(
        title: String,
        markdown: String,
        nowMillis: Long,
    ): MarkdownExportFile
}

@Singleton
class AndroidMarkdownFileWriter @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MarkdownFileWriter {
    override fun writeMarkdown(
        title: String,
        markdown: String,
        nowMillis: Long,
    ): MarkdownExportFile {
        val exportDir = File(context.filesDir, ExportDirectoryName)
        exportDir.mkdirs()

        val fileName = "${title.toSafeFileName()}-$nowMillis.md"
        val file = File(exportDir, fileName)
        file.writeText(markdown)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        return MarkdownExportFile(
            uriString = uri.toString(),
            fileName = fileName,
        )
    }

    private fun String.toSafeFileName(): String {
        val safeName = trim()
            .replace(UnsafeFileNameRegex, "_")
            .trim('_')
        return safeName.ifBlank { "article" }.take(MaxFileNameTitleLength)
    }

    private companion object {
        const val ExportDirectoryName = "exports"
        const val MaxFileNameTitleLength = 80
        val UnsafeFileNameRegex = Regex("""[^\p{L}\p{N}._-]+""")
    }
}
