package jp.hotdrop.createblogsupporter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM article_drafts ORDER BY updatedAt DESC")
    fun observeArticleDrafts(): Flow<List<ArticleDraftEntity>>

    @Query("SELECT * FROM article_drafts WHERE id = :articleId")
    fun observeArticleDraft(articleId: Long): Flow<ArticleDraftEntity?>

    @Query("SELECT * FROM article_drafts WHERE id = :articleId")
    suspend fun getArticleDraft(articleId: Long): ArticleDraftEntity?

    @Query("SELECT * FROM article_sections WHERE articleId = :articleId ORDER BY orderIndex ASC")
    suspend fun getArticleSections(articleId: Long): List<ArticleSectionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertArticleDraft(articleDraft: ArticleDraftEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertArticleSections(articleSections: List<ArticleSectionEntity>)

    @Update
    suspend fun updateArticleDraft(articleDraft: ArticleDraftEntity)

    @Transaction
    suspend fun adoptOutlineProposal(
        articleId: Long,
        title: String,
        headings: List<String>,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase1) {
            return false
        }
        updateArticleDraft(
            current.copy(
                phase = ArticlePhase.Phase2,
                title = title,
                updatedAt = nowMillis,
            ),
        )
        insertArticleSections(
            headings.mapIndexed { index, heading ->
                ArticleSectionEntity(
                    articleId = articleId,
                    heading = heading,
                    orderIndex = index,
                    content = "",
                    draftContent = "",
                    proofreadStatus = ProofreadStatus.Unchecked,
                    proofreadMessage = null,
                    userApproved = false,
                    createdAt = nowMillis,
                    updatedAt = nowMillis,
                    lastSavedAt = null,
                    draftUpdatedAt = null,
                )
            },
        )
        return true
    }
}
