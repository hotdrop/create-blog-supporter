package jp.hotdrop.createblogsupporter.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus

@Entity(tableName = "article_drafts")
data class ArticleDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phase: ArticlePhase,
    val title: String,
    val topic: String,
    val detail: String,
    val status: ArticleStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val exportedAt: Long?,
)

data class ArticleDraftSummaryEntity(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
    val topic: String,
    val status: ArticleStatus,
    val updatedAt: Long,
    val allSectionsApproved: Boolean,
)

data class ArticleDraftHeaderEntity(
    val id: Long,
    val phase: ArticlePhase,
    val title: String,
)

@Entity(
    tableName = "article_sections",
    foreignKeys = [
        ForeignKey(
            entity = ArticleDraftEntity::class,
            parentColumns = ["id"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["articleId", "orderIndex"]),
    ],
)
data class ArticleSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val articleId: Long,
    val heading: String,
    val orderIndex: Int,
    val content: String,
    val draftContent: String,
    val proofreadStatus: ProofreadStatus,
    val proofreadMessage: String?,
    val userApproved: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSavedAt: Long?,
    val draftUpdatedAt: Long?,
)
