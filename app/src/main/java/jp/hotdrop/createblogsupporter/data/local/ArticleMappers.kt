package jp.hotdrop.createblogsupporter.data.local

import jp.hotdrop.createblogsupporter.domain.model.ArticleDraft
import jp.hotdrop.createblogsupporter.domain.model.ArticleSection

fun ArticleDraftEntity.toDomain(): ArticleDraft =
    ArticleDraft(
        id = id,
        phase = phase,
        title = title,
        topic = topic,
        detail = detail,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        exportedAt = exportedAt,
    )

fun ArticleSectionEntity.toDomain(): ArticleSection =
    ArticleSection(
        id = id,
        articleId = articleId,
        heading = heading,
        orderIndex = orderIndex,
        content = content,
        draftContent = draftContent,
        proofreadStatus = proofreadStatus,
        proofreadMessage = proofreadMessage,
        userApproved = userApproved,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSavedAt = lastSavedAt,
        draftUpdatedAt = draftUpdatedAt,
    )
