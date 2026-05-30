package jp.hotdrop.createblogsupporter.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query(
        """
        SELECT
            id,
            phase,
            title,
            topic,
            status,
            updatedAt,
            CASE
                WHEN phase = 'phase2'
                    AND EXISTS (
                        SELECT 1 FROM article_sections
                        WHERE article_sections.articleId = article_drafts.id
                    )
                    AND NOT EXISTS (
                        SELECT 1 FROM article_sections
                        WHERE article_sections.articleId = article_drafts.id
                            AND article_sections.userApproved = 0
                    )
                THEN 1
                ELSE 0
            END AS allSectionsApproved
        FROM article_drafts
        ORDER BY updatedAt DESC
        """,
    )
    fun observeArticleDraftSummaries(): Flow<List<ArticleDraftSummaryEntity>>

    @Query("SELECT * FROM article_drafts WHERE id = :articleId")
    fun observeArticleDraft(articleId: Long): Flow<ArticleDraftEntity?>

    @Query("SELECT id, phase, title FROM article_drafts WHERE id = :articleId")
    fun observeArticleDraftHeader(articleId: Long): Flow<ArticleDraftHeaderEntity?>

    @Query("SELECT * FROM article_drafts WHERE id = :articleId")
    suspend fun getArticleDraft(articleId: Long): ArticleDraftEntity?

    @Query("SELECT id, phase, title FROM article_drafts WHERE id = :articleId")
    suspend fun getArticleDraftHeader(articleId: Long): ArticleDraftHeaderEntity?

    @Query("SELECT * FROM article_sections WHERE articleId = :articleId ORDER BY orderIndex ASC")
    suspend fun getArticleSections(articleId: Long): List<ArticleSectionEntity>

    @Query("SELECT * FROM article_sections WHERE articleId = :articleId ORDER BY orderIndex ASC")
    fun observeArticleSections(articleId: Long): Flow<List<ArticleSectionEntity>>

    @Query("SELECT * FROM article_sections WHERE articleId = :articleId AND id = :sectionId")
    fun observeArticleSection(articleId: Long, sectionId: Long): Flow<ArticleSectionEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertArticleDraft(articleDraft: ArticleDraftEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertArticleSection(articleSection: ArticleSectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertArticleSections(articleSections: List<ArticleSectionEntity>)

    @Update
    suspend fun updateArticleDraft(articleDraft: ArticleDraftEntity)

    @Update
    suspend fun updateArticleSection(articleSection: ArticleSectionEntity)

    @Delete
    suspend fun deleteArticleSection(articleSection: ArticleSectionEntity)

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

    @Transaction
    suspend fun updatePhase2Title(
        articleId: Long,
        title: String,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        updateArticleDraft(
            current.copy(
                title = title,
                updatedAt = nowMillis,
            ),
        )
        return true
    }

    @Transaction
    suspend fun addArticleSection(
        articleId: Long,
        heading: String,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val nextOrderIndex = getArticleSections(articleId).maxOfOrNull { it.orderIndex }?.plus(1) ?: 0
        insertArticleSection(
            ArticleSectionEntity(
                articleId = articleId,
                heading = heading,
                orderIndex = nextOrderIndex,
                content = "",
                draftContent = "",
                proofreadStatus = ProofreadStatus.Unchecked,
                proofreadMessage = null,
                userApproved = false,
                createdAt = nowMillis,
                updatedAt = nowMillis,
                lastSavedAt = null,
                draftUpdatedAt = null,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun updateArticleSectionHeading(
        articleId: Long,
        sectionId: Long,
        heading: String,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val section = getArticleSections(articleId).firstOrNull { it.id == sectionId } ?: return false
        updateArticleSection(
            section.copy(
                heading = heading,
                updatedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun deleteArticleSection(
        articleId: Long,
        sectionId: Long,
        nowMillis: Long,
    ): DeleteArticleSectionDaoResult {
        val current = getArticleDraft(articleId) ?: return DeleteArticleSectionDaoResult.NotFoundOrNotPhase2
        if (current.phase != ArticlePhase.Phase2) {
            return DeleteArticleSectionDaoResult.NotFoundOrNotPhase2
        }
        val sections = getArticleSections(articleId)
        if (sections.size <= 1) {
            return DeleteArticleSectionDaoResult.LastSection
        }
        val section = sections.firstOrNull { it.id == sectionId }
            ?: return DeleteArticleSectionDaoResult.NotFoundOrNotPhase2
        deleteArticleSection(section)
        sections
            .filterNot { it.id == sectionId }
            .sortedBy { it.orderIndex }
            .forEachIndexed { index, remainingSection ->
                if (remainingSection.orderIndex != index) {
                    updateArticleSection(
                        remainingSection.copy(
                            orderIndex = index,
                            updatedAt = nowMillis,
                        ),
                    )
                }
            }
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return DeleteArticleSectionDaoResult.Deleted
    }

    @Transaction
    suspend fun moveArticleSection(
        articleId: Long,
        sectionId: Long,
        direction: ArticleSectionMoveDirection,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val sections = getArticleSections(articleId)
        val currentIndex = sections.indexOfFirst { it.id == sectionId }
        if (currentIndex == -1) {
            return false
        }
        val targetIndex = when (direction) {
            ArticleSectionMoveDirection.Up -> currentIndex - 1
            ArticleSectionMoveDirection.Down -> currentIndex + 1
        }
        val target = sections.getOrNull(targetIndex) ?: return false
        val moving = sections[currentIndex]
        updateArticleSection(
            moving.copy(
                orderIndex = target.orderIndex,
                updatedAt = nowMillis,
            ),
        )
        updateArticleSection(
            target.copy(
                orderIndex = moving.orderIndex,
                updatedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun updateArticleSectionDraftContent(
        articleId: Long,
        sectionId: Long,
        draftContent: String,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val section = getArticleSections(articleId).firstOrNull { it.id == sectionId } ?: return false
        updateArticleSection(
            section.copy(
                draftContent = draftContent,
                updatedAt = nowMillis,
                draftUpdatedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun saveArticleSectionContent(
        articleId: Long,
        sectionId: Long,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val section = getArticleSections(articleId).firstOrNull { it.id == sectionId } ?: return false
        updateArticleSection(
            section.copy(
                content = section.draftContent,
                userApproved = false,
                updatedAt = nowMillis,
                lastSavedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun resetArticleSectionDraftToSaved(
        articleId: Long,
        sectionId: Long,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val section = getArticleSections(articleId).firstOrNull { it.id == sectionId } ?: return false
        updateArticleSection(
            section.copy(
                draftContent = section.content,
                updatedAt = nowMillis,
                draftUpdatedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun updateArticleSectionUserApproved(
        articleId: Long,
        sectionId: Long,
        userApproved: Boolean,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        val section = getArticleSections(articleId).firstOrNull { it.id == sectionId } ?: return false
        updateArticleSection(
            section.copy(
                userApproved = userApproved,
                updatedAt = nowMillis,
            ),
        )
        updateArticleDraft(current.copy(updatedAt = nowMillis))
        return true
    }

    @Transaction
    suspend fun markArticleExported(
        articleId: Long,
        nowMillis: Long,
    ): Boolean {
        val current = getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase2) {
            return false
        }
        updateArticleDraft(
            current.copy(
                status = ArticleStatus.Exported,
                updatedAt = nowMillis,
                exportedAt = nowMillis,
            ),
        )
        return true
    }
}

enum class DeleteArticleSectionDaoResult {
    Deleted,
    LastSection,
    NotFoundOrNotPhase2,
}
