package jp.hotdrop.createblogsupporter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ArticleDraftEntity::class,
        ArticleSectionEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(ArticleConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
