package jp.hotdrop.createblogsupporter.data.repository

import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftEntity
import jp.hotdrop.createblogsupporter.data.local.toDomain
import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
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
}
