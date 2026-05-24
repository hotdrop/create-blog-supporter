package jp.hotdrop.createblogsupporter.data.local

import androidx.room.TypeConverter
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus

class ArticleConverters {
    @TypeConverter
    fun articlePhaseToString(value: ArticlePhase): String = value.storageValue

    @TypeConverter
    fun stringToArticlePhase(value: String): ArticlePhase =
        ArticlePhase.entries.first { it.storageValue == value }

    @TypeConverter
    fun articleStatusToString(value: ArticleStatus): String = value.storageValue

    @TypeConverter
    fun stringToArticleStatus(value: String): ArticleStatus =
        ArticleStatus.entries.first { it.storageValue == value }

    @TypeConverter
    fun proofreadStatusToString(value: ProofreadStatus): String = value.storageValue

    @TypeConverter
    fun stringToProofreadStatus(value: String): ProofreadStatus =
        ProofreadStatus.entries.first { it.storageValue == value }
}
