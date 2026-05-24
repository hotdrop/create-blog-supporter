package jp.hotdrop.createblogsupporter.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import jp.hotdrop.createblogsupporter.ui.articleentry.ArticleEntryRoute
import jp.hotdrop.createblogsupporter.ui.articlelist.ArticleListRoute
import jp.hotdrop.createblogsupporter.ui.articlememo.ArticleMemoRoute
import jp.hotdrop.createblogsupporter.ui.navigation.AppDestination
import jp.hotdrop.createblogsupporter.ui.outlineedit.OutlineEditRoute
import jp.hotdrop.createblogsupporter.ui.outlineproposal.OutlineProposalRoute
import jp.hotdrop.createblogsupporter.ui.sectioneditor.SectionEditorRoute
import jp.hotdrop.createblogsupporter.ui.theme.CreateBlogSupporterTheme

@Composable
fun CreateBlogSupporterApp() {
    CreateBlogSupporterTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = AppDestination.ArticleList,
        ) {
            composable(AppDestination.ArticleList) {
                ArticleListRoute(
                    onCreateArticle = { navController.navigate(AppDestination.NewArticle) },
                    onOpenArticle = { articleId ->
                        navController.navigate(AppDestination.editArticle(articleId))
                    },
                )
            }
            composable(AppDestination.NewArticle) {
                ArticleMemoRoute(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onGenerateOutline = { articleId ->
                        navController.navigate(AppDestination.outlineProposals(articleId))
                    },
                )
            }
            composable(
                route = AppDestination.EditArticlePattern,
                arguments = listOf(navArgument("articleId") { type = NavType.LongType }),
            ) {
                ArticleEntryRoute(
                    onBack = { navController.popBackStack() },
                    onPhase1Saved = { navController.popBackStack() },
                    onGenerateOutline = { articleId ->
                        navController.navigate(AppDestination.outlineProposals(articleId))
                    },
                    onEditOutline = { articleId ->
                        navController.navigate(AppDestination.outlineEdit(articleId))
                    },
                    onEditSection = { articleId, sectionId ->
                        navController.navigate(AppDestination.sectionEdit(articleId, sectionId))
                    },
                )
            }
            composable(
                route = AppDestination.OutlineProposalsPattern,
                arguments = listOf(navArgument("articleId") { type = NavType.LongType }),
            ) {
                OutlineProposalRoute(
                    onBack = { navController.popBackStack() },
                    onAdopted = { articleId ->
                        navController.navigate(AppDestination.editArticle(articleId)) {
                            popUpTo(AppDestination.ArticleList) {
                                inclusive = false
                            }
                        }
                    },
                )
            }
            composable(
                route = AppDestination.OutlineEditPattern,
                arguments = listOf(navArgument("articleId") { type = NavType.LongType }),
            ) {
                OutlineEditRoute(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = AppDestination.SectionEditPattern,
                arguments = listOf(
                    navArgument("articleId") { type = NavType.LongType },
                    navArgument("sectionId") { type = NavType.LongType },
                ),
            ) {
                SectionEditorRoute(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
