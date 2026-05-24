package jp.hotdrop.createblogsupporter.domain.validation

object ArticleValidation {
    fun normalizeTopic(topic: String): String = topic.trim()

    fun normalizeDetail(detail: String): String = detail.trim()

    fun isValidTopic(topic: String): Boolean = normalizeTopic(topic).isNotEmpty()
}
