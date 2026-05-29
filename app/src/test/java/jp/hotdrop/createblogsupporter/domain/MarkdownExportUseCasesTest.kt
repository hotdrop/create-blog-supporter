package jp.hotdrop.createblogsupporter.domain

import jp.hotdrop.createblogsupporter.data.export.MarkdownExportFile
import jp.hotdrop.createblogsupporter.data.export.MarkdownFileWriter
import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftHeaderEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleDraftSummaryEntity
import jp.hotdrop.createblogsupporter.data.local.ArticleSectionEntity
import jp.hotdrop.createblogsupporter.data.repository.ArticleRepository
import jp.hotdrop.createblogsupporter.domain.model.ArticlePhase
import jp.hotdrop.createblogsupporter.domain.model.ArticleSectionMoveDirection
import jp.hotdrop.createblogsupporter.domain.model.ArticleStatus
import jp.hotdrop.createblogsupporter.domain.model.ProofreadStatus
import jp.hotdrop.createblogsupporter.domain.usecase.ExportMarkdownResult
import jp.hotdrop.createblogsupporter.domain.usecase.ExportMarkdownUseCase
import jp.hotdrop.createblogsupporter.domain.usecase.GenerateMarkdownUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownExportUseCasesTest {
    @Test
    fun generateMarkdown_usesContentOnlyAndSortsByOrderIndex() {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraftBlocking(phase2Article())
        dao.insertArticleSectionsBlocking(
            listOf(
                section(
                    articleId = articleId,
                    heading = "まとめ",
                    orderIndex = 1,
                    content = "保存済み本文B",
                    draftContent = "混ぜてはいけない下書きB",
                    userApproved = true,
                ),
                section(
                    articleId = articleId,
                    heading = "背景",
                    orderIndex = 0,
                    content = "保存済み本文A",
                    draftContent = "混ぜてはいけない下書きA",
                    userApproved = true,
                ),
            ),
        )
        val sections = runBlocking { ArticleRepository(dao).getArticleSections(articleId) }

        val markdown = GenerateMarkdownUseCase()(
            title = "記事タイトル",
            sections = sections,
        )

        assertEquals(
            """
            # 記事タイトル

            ## 背景

            保存済み本文A

            ## まとめ

            保存済み本文B

            """.trimIndent(),
            markdown,
        )
        assertFalse(markdown.contains("混ぜてはいけない下書き"))
    }

    @Test
    fun exportMarkdown_rejectsBlankTitle() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article().copy(title = "   "))
        dao.insertArticleSections(listOf(section(articleId = articleId, userApproved = true)))
        val writer = FakeMarkdownFileWriter()
        val useCase = exportUseCase(dao, writer)

        val result = useCase(articleId)

        assertEquals(ExportMarkdownResult.BlankTitle, result)
        assertFalse(writer.wasCalled)
    }

    @Test
    fun exportMarkdown_rejectsMissingOrNonPhase2Article() = runBlocking {
        val dao = FakeArticleDao()
        val phase1Id = dao.insertArticleDraft(phase1Article())
        val writer = FakeMarkdownFileWriter()
        val useCase = exportUseCase(dao, writer)

        assertEquals(ExportMarkdownResult.NotPhase2OrMissing, useCase(999))
        assertEquals(ExportMarkdownResult.NotPhase2OrMissing, useCase(phase1Id))
        assertFalse(writer.wasCalled)
    }

    @Test
    fun exportMarkdown_rejectsUnapprovedSections() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(articleId = articleId, orderIndex = 0, userApproved = true),
                section(articleId = articleId, orderIndex = 1, userApproved = false),
            ),
        )
        val writer = FakeMarkdownFileWriter()
        val useCase = exportUseCase(dao, writer)

        val result = useCase(articleId)

        assertEquals(ExportMarkdownResult.UnapprovedSections(1), result)
        assertFalse(writer.wasCalled)
    }

    @Test
    fun exportMarkdown_returnsWriteFailed_whenFileWriterFails() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(listOf(section(articleId = articleId, userApproved = true)))
        val useCase = exportUseCase(
            dao = dao,
            writer = FakeMarkdownFileWriter(shouldFail = true),
        )

        val result = useCase(articleId)

        assertEquals(ExportMarkdownResult.WriteFailed, result)
        assertEquals(ArticleStatus.Draft, dao.getArticleDraft(articleId)?.status)
    }

    @Test
    fun exportMarkdown_writesFileAndMarksArticleExported() = runBlocking {
        val dao = FakeArticleDao()
        val articleId = dao.insertArticleDraft(phase2Article())
        dao.insertArticleSections(
            listOf(
                section(
                    articleId = articleId,
                    heading = "背景",
                    content = "保存済み本文",
                    draftContent = "未保存下書き",
                    userApproved = true,
                ),
            ),
        )
        val writer = FakeMarkdownFileWriter()
        val useCase = exportUseCase(dao, writer)

        val result = useCase(articleId)

        assertTrue(result is ExportMarkdownResult.Exported)
        assertTrue(writer.wasCalled)
        assertEquals("既存タイトル", writer.title)
        assertTrue(writer.markdown?.contains("保存済み本文") == true)
        assertFalse(writer.markdown?.contains("未保存下書き") == true)
        assertEquals(ArticleStatus.Exported, dao.getArticleDraft(articleId)?.status)
        assertEquals(writer.nowMillis, dao.getArticleDraft(articleId)?.exportedAt)
    }

    private fun exportUseCase(
        dao: FakeArticleDao,
        writer: MarkdownFileWriter,
    ): ExportMarkdownUseCase =
        ExportMarkdownUseCase(
            articleRepository = ArticleRepository(dao),
            generateMarkdownUseCase = GenerateMarkdownUseCase(),
            markdownFileWriter = writer,
        )

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
        heading: String = "背景",
        orderIndex: Int = 0,
        content: String = "保存済み本文",
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

private class FakeMarkdownFileWriter(
    private val shouldFail: Boolean = false,
) : MarkdownFileWriter {
    var wasCalled = false
    var title: String? = null
    var markdown: String? = null
    var nowMillis: Long? = null

    override fun writeMarkdown(
        title: String,
        markdown: String,
        nowMillis: Long,
    ): MarkdownExportFile {
        wasCalled = true
        if (shouldFail) {
            error("write failed")
        }
        this.title = title
        this.markdown = markdown
        this.nowMillis = nowMillis
        return MarkdownExportFile(
            uriString = "content://markdown/export.md",
            fileName = "export.md",
        )
    }
}

private class FakeArticleDao : ArticleDao {
    private var nextArticleId = 1L
    private var nextSectionId = 1L
    private val drafts = mutableMapOf<Long, ArticleDraftEntity>()
    private val sections = mutableListOf<ArticleSectionEntity>()

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

    fun insertArticleDraftBlocking(articleDraft: ArticleDraftEntity): Long =
        runBlocking { insertArticleDraft(articleDraft) }

    fun insertArticleSectionsBlocking(articleSections: List<ArticleSectionEntity>) {
        runBlocking { insertArticleSections(articleSections) }
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
