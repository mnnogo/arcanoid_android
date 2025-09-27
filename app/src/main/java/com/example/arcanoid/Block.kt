package com.example.arcanoid

enum class BlockType {
    NORMAL,
    BONUS_EXTRA_BALL,
    BONUS_WIDER_PADDLE
}

data class Block(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: BlockType = BlockType.NORMAL,
    var isDestroyed: Boolean = false
)