package jp.hotdrop.createblogsupporter.data

import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleSectionEntity
import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleRepositoryTest {
    @Test
    fun adoptOutlineProposal_updatesPhaseTitleAndInitialSections_forPhase1Article() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase1Article())
        val repository = ArticleRepository(dao)

        val adopted = repository.adoptOutlineProposal(
            articleId = articleId,
            title = "採用タイトル",
            headings = listOf("背景", "設計", "実装", "まとめ"),
            nowMillis = 200,
        )

        val article = dao.getArticleDraft(articleId)
        val sections = dao.getArticleSections(articleId)
        assertTrue(adopted)
        assertEquals(ArticlePhase.Phase2, article?.phase)
        assertEquals("採用タイトル", article?.title)
        assertEquals(200L, article?.updatedAt)
        assertEquals(listOf(0, 1, 2, 3), sections.map { it.orderIndex })
        assertEquals(listOf("背景", "設計", "実装", "まとめ"), sections.map { it.heading })
        assertTrue(sections.all { it.content.isEmpty() })
        assertTrue(sections.all { it.draftContent.isEmpty() })
        assertTrue(sections.all { it.proofreadStatus == ProofreadStatus.Unchecked })
        assertTrue(sections.none { it.userApproved })
        assertTrue(sections.all { it.lastSavedAt == null })
        assertTrue(sections.all { it.draftUpdatedAt == null })
    }

    @Test
    fun adoptOutlineProposal_failsAndDoesNotInsertSections_whenArticleMissing() = runBlocking {
        val dao = FakeArticleDao()
        val repository = ArticleRepository(dao)

        val adopted = repository.adoptOutlineProposal(
            articleId = 999,
            title = "採用タイトル",
            headings = listOf("背景"),
            nowMillis = 200,
        )

        assertFalse(adopted)
        assertTrue(dao.sections.isEmpty())
    }

    @Test
    fun adoptOutlineProposal_failsAndDoesNotInsertSections_whenArticleIsAlreadyPhase2() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(
            phase1Article().copy(
                phase = ArticlePhase.Phase2,
                title = "既存タイトル",
            ),
        )
        val repository = ArticleRepository(dao)

        val adopted = repository.adoptOutlineProposal(
            articleId = articleId,
            title = "採用タイトル",
            headings = listOf("背景"),
            nowMillis = 200,
        )

        assertFalse(adopted)
        assertEquals(ArticlePhase.Phase2, dao.getArticleDraft(articleId)?.phase)
        assertEquals("既存タイトル", dao.getArticleDraft(articleId)?.title)
        assertTrue(dao.getArticleSections(articleId).isEmpty())
    }

    private fun phase1Article(): ArticleDraftEntity =
        ArticleDraftEntity(
            phase = ArticlePhase.Phase1,
            title = "",
            topic = "Compose Navigation",
            detail = "RouteとScreenを分ける",
            status = ArticleStatus.Draft,
            createdAt = 100,
            updatedAt = 100,
            exportedAt = null,
        )
}

private class FakeArticleDao : ArticleDao {
    private var nextArticleId = 1L
    private var nextSectionId = 1L
    private val drafts = mutableMapOf<Long, ArticleDraftEntity>()
    val sections = mutableListOf<ArticleSectionEntity>()

    override fun observeArticleDrafts(): Flow<List<ArticleDraftEntity>> =
        flowOf(drafts.values.toList())

    override fun observeArticleDraft(articleId: Long): Flow<ArticleDraftEntity?> =
        flowOf(drafts[articleId])

    override suspend fun getArticleDraft(articleId: Long): ArticleDraftEntity? =
        drafts[articleId]

    override suspend fun getArticleSections(articleId: Long): List<ArticleSectionEntity> =
        sections.filter { it.articleId == articleId }.sortedBy { it.orderIndex }

    override suspend fun insertArticleDraft(articleDraft: ArticleDraftEntity): Long {
        val id = nextArticleId++
        drafts[id] = articleDraft.copy(id = id)
        return id
    }

    override suspend fun insertArticleSections(articleSections: List<ArticleSectionEntity>) {
        sections += articleSections.map { section ->
            section.copy(id = nextSectionId++)
        }
    }

    override suspend fun updateArticleDraft(articleDraft: ArticleDraftEntity) {
        if (drafts.containsKey(articleDraft.id)) {
            drafts[articleDraft.id] = articleDraft
        }
    }
}
