package jp.hotdrop.createblogsupporter.ui.navigation

object AppDestination {
    const val ArticleList = "article_list"
    const val NewArticle = "article/new"
    const val EditArticlePattern = "article/{articleId}"
    const val OutlineProposalsPattern = "article/{articleId}/outline-proposals"

    fun editArticle(articleId: Long): String = "article/$articleId"

    fun outlineProposals(articleId: Long): String = "article/$articleId/outline-proposals"
}
