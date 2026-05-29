package jp.hotdrop.createblogsupporter.data

import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftHeaderEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftSummaryEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleSectionEntity
import jp.hotdrop.createblogsupporter.data.local.DeleteArticleSectionDaoResult
import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.usecase.ArticleSectionContentOperationResult
import jp.hotdrop.createblogsupporter.domain.usecase.SaveArticleSectionContentUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleResult
import jp.hotdrop.createblogsupporter.domain.usecase.UpdatePhase2TitleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleRepositoryTest {
    @Test
    fun observeArticleDraftSummaries_returnsListFieldsWithoutDetail() = runBlocking {
        val dao = FakeArticleDao()
        val phase1Id = dao.insertArticleDraft(
            phase1Article().copy(
                topic = "Phase1 topic",
                detail = "目次案生成用の詳細",
                updatedAt = 100,
            ),
        )
        val phase2Id = dao.insertArticleDraft(
            phase2Article().copy(
                title = "Phase2 title",
                topic = "Phase2 topic",
                detail = "Phase2では表示しない詳細",
                updatedAt = 200,
            ),
        )
        val repository = ArticleRepository(dao)

        val summaries = repository.observeArticleDraftSummaries().first()

        assertEquals(listOf(phase2Id, phase1Id), summaries.map { it.id })
        assertEquals(listOf(ArticlePhase.Phase2, ArticlePhase.Phase1), summaries.map { it.phase })
        assertEquals(listOf("Phase2 title", ""), summaries.map { it.title })
        assertEquals(listOf("Phase2 topic", "Phase1 topic"), summaries.map { it.topic })
        assertEquals(listOf(200L, 100L), summaries.map { it.updatedAt })
    }

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

    @Test
    fun addArticleSection_appendsSectionWithInitialContentState() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(section(articleId = articleId, heading = "背景", orderIndex = 0)),
        )
        val repository = ArticleRepository(dao)

        val added = repository.addArticleSection(
            articleId = articleId,
            heading = "まとめ",
            nowMillis = 300,
        )

        val sections = dao.getArticleSections(articleId)
        val addedSection = sections.last()
        assertTrue(added)
        assertEquals(listOf(0, 1), sections.map { it.orderIndex })
        assertEquals("まとめ", addedSection.heading)
        assertEquals("", addedSection.content)
        assertEquals("", addedSection.draftContent)
        assertFalse(addedSection.userApproved)
        assertEquals(ProofreadStatus.Unchecked, addedSection.proofreadStatus)
        assertEquals(300L, dao.getArticleDraft(articleId)?.updatedAt)
    }

    @Test
    fun updateArticleSectionHeading_keepsContentDraftAndApprovalState() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "変更前",
                    orderIndex = 0,
                    content = "保存済み本文",
                    draftContent = "編集中本文",
                    userApproved = true,
                ),
            ),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val updated = repository.updateArticleSectionHeading(
            articleId = articleId,
            sectionId = sectionId,
            heading = "変更後",
            nowMillis = 300,
        )

        val section = dao.getArticleSections(articleId).single()
        assertTrue(updated)
        assertEquals("変更後", section.heading)
        assertEquals("保存済み本文", section.content)
        assertEquals("編集中本文", section.draftContent)
        assertTrue(section.userApproved)
    }

    @Test
    fun deleteArticleSection_reordersRemainingSectionsFromZero() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(articleId = articleId, heading = "背景", orderIndex = 0),
                section(articleId = articleId, heading = "設計", orderIndex = 1),
                section(articleId = articleId, heading = "まとめ", orderIndex = 2),
            ),
        )
        val deleteSectionId = dao.getArticleSections(articleId)[1].id
        val repository = ArticleRepository(dao)

        val result = repository.deleteArticleSection(
            articleId = articleId,
            sectionId = deleteSectionId,
            nowMillis = 300,
        )

        val sections = dao.getArticleSections(articleId)
        assertEquals(DeleteArticleSectionDaoResult.Deleted, result)
        assertEquals(listOf("背景", "まとめ"), sections.map { it.heading })
        assertEquals(listOf(0, 1), sections.map { it.orderIndex })
    }

    @Test
    fun deleteArticleSection_failsWhenOnlyOneSectionRemains() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(section(articleId = articleId, heading = "背景", orderIndex = 0)),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val result = repository.deleteArticleSection(
            articleId = articleId,
            sectionId = sectionId,
            nowMillis = 300,
        )

        assertEquals(DeleteArticleSectionDaoResult.LastSection, result)
        assertEquals(listOf("背景"), dao.getArticleSections(articleId).map { it.heading })
    }

    @Test
    fun moveArticleSection_swapsOnlyOrderIndexWithNeighbor() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(articleId = articleId, heading = "背景", orderIndex = 0, content = "A"),
                section(articleId = articleId, heading = "設計", orderIndex = 1, content = "B"),
            ),
        )
        val sectionId = dao.getArticleSections(articleId)[1].id
        val repository = ArticleRepository(dao)

        val moved = repository.moveArticleSection(
            articleId = articleId,
            sectionId = sectionId,
            direction = ArticleSectionMoveDirection.Up,
            nowMillis = 300,
        )

        val sections = dao.getArticleSections(articleId)
        assertTrue(moved)
        assertEquals(listOf("設計", "背景"), sections.map { it.heading })
        assertEquals(listOf("B", "A"), sections.map { it.content })
        assertEquals(listOf(0, 1), sections.map { it.orderIndex })
    }

    @Test
    fun updatePhase2Title_rejectsMissingPhase1AndBlankTitleHandledByUseCase() = runBlocking {
        val dao = FakeArticleDao()
        val phase1Id = dao.insertArticleDraft(phase1Article())
        val phase2Id = dao.insertArticleDraft(phase2Article())
        val repository = ArticleRepository(dao)

        val missing = repository.updatePhase2Title(
            articleId = 999,
            title = "missing",
            nowMillis = 300,
        )
        val phase1 = repository.updatePhase2Title(
            articleId = phase1Id,
            title = "phase1",
            nowMillis = 300,
        )
        val phase2 = repository.updatePhase2Title(
            articleId = phase2Id,
            title = "phase2",
            nowMillis = 300,
        )

        assertFalse(missing)
        assertFalse(phase1)
        assertTrue(phase2)
        assertEquals("phase2", dao.getArticleDraft(phase2Id)?.title)
    }

    @Test
    fun updatePhase2TitleUseCase_rejectsBlankTitle() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        val useCase = UpdatePhase2TitleUseCase(ArticleRepository(dao))

        val result = useCase(articleId, "   ")

        assertEquals(UpdatePhase2TitleResult.InvalidTitle, result)
        assertEquals("既存タイトル", dao.getArticleDraft(articleId)?.title)
    }

    @Test
    fun updateArticleSectionDraftContent_updatesOnlyDraftContent() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "背景",
                    orderIndex = 0,
                    content = "保存済み本文",
                    draftContent = "古い下書き",
                    userApproved = true,
                ),
            ),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val updated = repository.updateArticleSectionDraftContent(
            articleId = articleId,
            sectionId = sectionId,
            draftContent = "新しい下書き",
            nowMillis = 400,
        )

        val section = dao.getArticleSections(articleId).single()
        assertTrue(updated)
        assertEquals("保存済み本文", section.content)
        assertEquals("新しい下書き", section.draftContent)
        assertTrue(section.userApproved)
        assertEquals(400L, section.draftUpdatedAt)
        assertEquals(null, section.lastSavedAt)
    }

    @Test
    fun saveArticleSectionContent_copiesDraftToContentAndClearsApproval() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "背景",
                    orderIndex = 0,
                    content = "保存済み本文",
                    draftContent = "保存する下書き",
                    userApproved = true,
                ),
            ),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val saved = repository.saveArticleSectionContent(
            articleId = articleId,
            sectionId = sectionId,
            nowMillis = 500,
        )

        val section = dao.getArticleSections(articleId).single()
        assertTrue(saved)
        assertEquals("保存する下書き", section.content)
        assertEquals("保存する下書き", section.draftContent)
        assertFalse(section.userApproved)
        assertEquals(500L, section.lastSavedAt)
    }

    @Test
    fun resetArticleSectionDraftToSaved_keepsContentAndCopiesItToDraft() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "背景",
                    orderIndex = 0,
                    content = "保存済み本文",
                    draftContent = "破棄する下書き",
                    userApproved = true,
                ),
            ),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val reset = repository.resetArticleSectionDraftToSaved(
            articleId = articleId,
            sectionId = sectionId,
            nowMillis = 600,
        )

        val section = dao.getArticleSections(articleId).single()
        assertTrue(reset)
        assertEquals("保存済み本文", section.content)
        assertEquals("保存済み本文", section.draftContent)
        assertTrue(section.userApproved)
        assertEquals(600L, section.draftUpdatedAt)
    }

    @Test
    fun updateArticleSectionUserApproved_doesNotChangeContentOrDraft() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "背景",
                    orderIndex = 0,
                    content = "保存済み本文",
                    draftContent = "編集中本文",
                ),
            ),
        )
        val sectionId = dao.getArticleSections(articleId).single().id
        val repository = ArticleRepository(dao)

        val updated = repository.updateArticleSectionUserApproved(
            articleId = articleId,
            sectionId = sectionId,
            userApproved = true,
            nowMillis = 700,
        )

        val section = dao.getArticleSections(articleId).single()
        assertTrue(updated)
        assertEquals("保存済み本文", section.content)
        assertEquals("編集中本文", section.draftContent)
        assertTrue(section.userApproved)
    }

    @Test
    fun articleSectionContentOperations_failForInvalidArticleOrSection() = runBlocking {
        val dao = FakeArticleDao()
        val phase1Id = dao.insertArticleDraft(phase1Article())
        val phase2Id = dao.insertArticleDraft(phase2Article())
        val otherArticleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(listOf(section(articleId = otherArticleId, heading = "他記事", orderIndex = 0)))
        val otherSectionId = dao.getArticleSections(otherArticleId).single().id
        val repository = ArticleRepository(dao)

        assertFalse(repository.updateArticleSectionDraftContent(phase1Id, otherSectionId, "本文", 800))
        assertFalse(repository.updateArticleSectionDraftContent(999, otherSectionId, "本文", 800))
        assertFalse(repository.updateArticleSectionDraftContent(phase2Id, 999, "本文", 800))
        assertFalse(repository.updateArticleSectionDraftContent(phase2Id, otherSectionId, "本文", 800))
        assertFalse(repository.saveArticleSectionContent(phase1Id, otherSectionId, 800))
        assertFalse(repository.resetArticleSectionDraftToSaved(phase1Id, otherSectionId, 800))
        assertFalse(repository.updateArticleSectionUserApproved(phase1Id, otherSectionId, true, 800))
    }

    @Test
    fun saveArticleSectionContentUseCase_returnsNotPhase2OrMissing_whenRepositoryRejects() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase1Article())
        val useCase = SaveArticleSectionContentUseCase(ArticleRepository(dao))

        val result = useCase(articleId, 999)

        assertEquals(ArticleSectionContentOperationResult.NotPhase2OrMissing, result)
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

    private fun phase2Article(): ArticleDraftEntity =
        phase1Article().copy(
            phase = ArticlePhase.Phase2,
            title = "既存タイトル",
        )

    private fun section(
        articleId: Long,
        heading: String,
        orderIndex: Int,
        content: String = "",
        draftContent: String = "",
        userApproved: Boolean = false,
    ): ArticleSectionEntity =
        ArticleSectionEntity(
            articleId = articleId,
            heading = heading,
            orderIndex = orderIndex,
            content = content,
            draftContent = draftContent,
            proofreadStatus = ProofreadStatus.Unchecked,
            proofreadMessage = null,
            userApproved = userApproved,
            createdAt = 100,
            updatedAt = 100,
            lastSavedAt = null,
            draftUpdatedAt = null,
        )
}

private class FakeArticleDao : ArticleDao {
    private var nextArticleId = 1L
    private var nextSectionId = 1L
    private val drafts = mutableMapOf<Long, ArticleDraftEntity>()
    val sections = mutableListOf<ArticleSectionEntity>()

    override fun observeArticleDraftSummaries(): Flow<List<ArticleDraftSummaryEntity>> =
        flowOf(drafts.values.sortedByDescending { it.updatedAt }.map { it.toSummaryEntity() })

    override fun observeArticleDraft(articleId: Long): Flow<ArticleDraftEntity?> =
        flowOf(drafts[articleId])

    override fun observeArticleDraftHeader(articleId: Long): Flow<ArticleDraftHeaderEntity?> =
        flowOf(drafts[articleId]?.toHeaderEntity())

    override suspend fun getArticleDraft(articleId: Long): ArticleDraftEntity? =
        drafts[articleId]

    override suspend fun getArticleDraftHeader(articleId: Long): ArticleDraftHeaderEntity? =
        drafts[articleId]?.toHeaderEntity()

    override suspend fun getArticleSections(articleId: Long): List<ArticleSectionEntity> =
        sections.filter { it.articleId == articleId }.sortedBy { it.orderIndex }

    override fun observeArticleSections(articleId: Long): Flow<List<ArticleSectionEntity>> =
        flowOf(sections.filter { it.articleId == articleId }.sortedBy { it.orderIndex })

    override fun observeArticleSection(articleId: Long, sectionId: Long): Flow<ArticleSectionEntity?> =
        flowOf(sections.firstOrNull { it.articleId == articleId && it.id == sectionId })

    override suspend fun insertArticleDraft(articleDraft: ArticleDraftEntity): Long {
        val id = nextArticleId++
        drafts[id] = articleDraft.copy(id = id)
        return id
    }

    override suspend fun insertArticleSection(articleSection: ArticleSectionEntity): Long {
        val id = nextSectionId++
        sections += articleSection.copy(id = id)
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

    override suspend fun updateArticleSection(articleSection: ArticleSectionEntity) {
        val index = sections.indexOfFirst { it.id == articleSection.id }
        if (index != -1) {
            sections[index] = articleSection
        }
    }

    override suspend fun deleteArticleSection(articleSection: ArticleSectionEntity) {
        sections.removeAll { it.id == articleSection.id }
    }

    private fun ArticleDraftEntity.toSummaryEntity(): ArticleDraftSummaryEntity =
        ArticleDraftSummaryEntity(
            id = id,
            phase = phase,
            title = title,
            topic = topic,
            status = status,
            updatedAt = updatedAt,
        )

    private fun ArticleDraftEntity.toHeaderEntity(): ArticleDraftHeaderEntity =
        ArticleDraftHeaderEntity(
            id = id,
            phase = phase,
            title = title,
        )
}
