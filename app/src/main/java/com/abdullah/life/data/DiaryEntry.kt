package com.abdullah.life.data

import androidx.room.*
import java.util.Date

enum class BlockType { TEXT, IMAGE, VIDEO, AUDIO }

data class ContentBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: BlockType,
    var content: String // Stores text content OR file URI for media
)

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val contentBlocksJson: String, // Ordered blocks of text, images, videos, audio
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    val isDraft: Boolean = false
)