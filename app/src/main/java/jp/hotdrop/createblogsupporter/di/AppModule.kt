package jp.hotdrop.createblogsupporter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.hotdrop.createblogsupporter.data.local.AppDatabase
import jp.hotdrop.createblogsupporter.data.local.ArticleDao
import jp.hotdrop.createblogsupporter.data.export.AndroidMarkdownFileWriter
import jp.hotdrop.createblogsupporter.data.export.MarkdownFileWriter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "create_blog_supporter.db",
        ).build()

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao = database.articleDao()

    @Provides
    @Singleton
    fun provideMarkdownFileWriter(writer: AndroidMarkdownFileWriter): MarkdownFileWriter = writer
}
