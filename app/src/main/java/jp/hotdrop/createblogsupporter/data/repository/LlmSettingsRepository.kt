package jp.hotdrop.createblogsupporter.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import jp.hotdrop.createblogsupporter.domain.model.LlmSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class LlmSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    fun observe(): Flow<LlmSettings> =
        callbackFlow {
            trySend(get())
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KeyModelDisplayName || key == KeyModelFilePath) {
                    trySend(get())
                }
            }
            preferences.registerOnSharedPreferenceChangeListener(listener)
            awaitClose {
                preferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }.distinctUntilChanged()

    fun get(): LlmSettings =
        LlmSettings(
            modelDisplayName = preferences.getString(KeyModelDisplayName, null),
            modelFilePath = preferences.getString(KeyModelFilePath, null),
        )

    fun saveModelSelection(selection: LlmModelFileSelection) {
        preferences.edit()
            .putString(KeyModelDisplayName, selection.displayName)
            .putString(KeyModelFilePath, selection.absolutePath)
            .apply()
    }

    fun importModelFile(uri: Uri): LlmModelFileSelection {
        val displayName = resolveDisplayName(uri)
        require(isLiteRtLmModelFileName(displayName)) {
            "LiteRT-LM model must have .litertlm extension"
        }

        val targetDir = File(context.filesDir, ModelDirectoryName).apply { mkdirs() }
        val targetFile = File(targetDir, sanitizeModelFileName(displayName))

        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Failed to open LiteRT-LM model file" }
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return LlmModelFileSelection(
            displayName = displayName,
            absolutePath = targetFile.absolutePath,
        )
    }

    private fun resolveDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(index)
                }
            }
        return uri.lastPathSegment?.substringAfterLast('/') ?: DefaultModelFileName
    }

    private companion object {
        private const val PreferencesName = "llm_settings"
        private const val KeyModelDisplayName = "model_display_name"
        private const val KeyModelFilePath = "model_file_path"
        private const val ModelDirectoryName = "litertlm-models"
        private const val DefaultModelFileName = "selected-model.litertlm"
    }
}

data class LlmModelFileSelection(
    val displayName: String,
    val absolutePath: String,
)

internal fun isLiteRtLmModelFileName(fileName: String): Boolean =
    fileName.endsWith(".litertlm", ignoreCase = true)

internal fun sanitizeModelFileName(fileName: String): String =
    fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
