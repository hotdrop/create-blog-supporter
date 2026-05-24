package jp.hotdrop.createblogsupporter.ui.navigation

object AppDestination {
    const val ArticleList = "article_list"
    const val NewArticle = "article/new"
    const val EditArticlePattern = "article/{articleId}"
    const val OutlineProposalsPattern = "article/{articleId}/outline-proposals"
    const val OutlineEditPattern = "article/{articleId}/outline-edit"
    const val SectionEditPattern = "article/{articleId}/sections/{sectionId}"

    fun editArticle(articleId: Long): String = "article/$articleId"

    fun outlineProposals(articleId: Long): String = "article/$articleId/outline-proposals"

    fun outlineEdit(articleId: Long): String = "article/$articleId/outline-edit"

    fun sectionEdit(articleId: Long, sectionId: Long): String = "article/$articleId/sections/$sectionId"
}
