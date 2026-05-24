package jp.hotdrop.createblogsupporter.data.repository

import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftEntity
import jp.hotdrop.createblogsupporter.data.local.DeleteArticleSectionDaoResult
import jp.hotdrop.createblogsupporter.data.local.toDomain
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
) {
    fun observeArticleDrafts(): Flow<List<ArticleDraft>> =
        articleDao.observeArticleDrafts().map { drafts -> drafts.map { it.toDomain() } }

    fun observeArticleDraft(articleId: Long): Flow<ArticleDraft?> =
        articleDao.observeArticleDraft(articleId).map { it?.toDomain() }

    fun observeArticleSections(articleId: Long): Flow<List<ArticleSection>> =
        articleDao.observeArticleSections(articleId).map { sections -> sections.map { it.toDomain() } }

    suspend fun createPhase1Article(
        topic: String,
        detail: String,
        nowMillis: Long,
    ): Long =
        articleDao.insertArticleDraft(
            ArticleDraftEntity(
                phase = ArticlePhase.Phase1,
                title = "",
                topic = topic,
                detail = detail,
                status = ArticleStatus.Draft,
                createdAt = nowMillis,
                updatedAt = nowMillis,
                exportedAt = null,
            ),
        )

    suspend fun updatePhase1Article(
        articleId: Long,
        topic: String,
        detail: String,
        nowMillis: Long,
    ): Boolean {
        val current = articleDao.getArticleDraft(articleId) ?: return false
        if (current.phase != ArticlePhase.Phase1) {
            return false
        }
        articleDao.updateArticleDraft(
            current.copy(
                topic = topic,
                detail = detail,
                updatedAt = nowMillis,
            ),
        )
        return true
    }

    suspend fun adoptOutlineProposal(
        articleId: Long,
        title: String,
        headings: List<String>,
        nowMillis: Long,
    ): Boolean =
        articleDao.adoptOutlineProposal(
            articleId = articleId,
            title = title,
            headings = headings,
            nowMillis = nowMillis,
        )

    suspend fun getArticleSections(articleId: Long) =
        articleDao.getArticleSections(articleId).map { it.toDomain() }

    suspend fun updatePhase2Title(
        articleId: Long,
        title: String,
        nowMillis: Long,
    ): Boolean =
        articleDao.updatePhase2Title(
            articleId = articleId,
            title = title,
            nowMillis = nowMillis,
        )

    suspend fun addArticleSection(
        articleId: Long,
        heading: String,
        nowMillis: Long,
    ): Boolean =
        articleDao.addArticleSection(
            articleId = articleId,
            heading = heading,
            nowMillis = nowMillis,
        )

    suspend fun updateArticleSectionHeading(
        articleId: Long,
        sectionId: Long,
        heading: String,
        nowMillis: Long,
    ): Boolean =
        articleDao.updateArticleSectionHeading(
            articleId = articleId,
            sectionId = sectionId,
            heading = heading,
            nowMillis = nowMillis,
        )

    suspend fun deleteArticleSection(
        articleId: Long,
        sectionId: Long,
        nowMillis: Long,
    ): DeleteArticleSectionDaoResult =
        articleDao.deleteArticleSection(
            articleId = articleId,
            sectionId = sectionId,
            nowMillis = nowMillis,
        )

    suspend fun moveArticleSection(
        articleId: Long,
        sectionId: Long,
        direction: ArticleSectionMoveDirection,
        nowMillis: Long,
    ): Boolean =
        articleDao.moveArticleSection(
            articleId = articleId,
            sectionId = sectionId,
            direction = direction,
            nowMillis = nowMillis,
        )
}
