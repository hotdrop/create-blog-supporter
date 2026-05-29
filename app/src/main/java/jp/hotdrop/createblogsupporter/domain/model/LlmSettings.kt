package jp.hotdrop.createblogsupporter.domain.model

data class LlmSettings(
    val modelDisplayName: String? = null,
    val modelFilePath: String? = null,
) {
    val isModelConfigured: Boolean
        get() = !modelFilePath.isNullOrBlank()
}
