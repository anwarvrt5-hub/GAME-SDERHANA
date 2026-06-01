package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.OffWhite

enum class GameMode(val id: String, val title: String, val emoji: String, val desc: String) {
    TETRATRO("tetratro", "Tetratro", "🃏", "The Balatro style. Jokers modify scores, high multipliers, target thresholds."),
    ELEMENTRIS("elementris", "Elementris", "🔮", "RPG element system. Clear rows with Element combos (Fire+Spark=Boom!) to survive."),
    COMBO_STACKER("combo_stacker", "Combo Stacker", "📈", "Build massive consecutive line clear combos. Buy Relics to bend physics."),
    DUNGEON_TETRIS("dungeon_tetris", "Dungeon Tetris", "⚔️", "Roguelike dungeon battler. Line clears trigger skills to slay monsters."),
    TETRIS_CASINO("tetris_casino", "Tetris Casino", "🎲", "Wager, double-or-nothing, or run risky S/Z modifiers for legendary score multipliers."),
    FACTORY_BLOCKS("factory_blocks", "Factory Blocks", "🏭", "Incremental automation. Blocks produce resources to auto-buy score machinery."),
    FUSION_TETRIS("fusion_tetris", "Fusion Tetris", "🧪", "Merge adjacently stacked blocks to level them up into high-multiplier star blocks.")
}

sealed class TetrominoShape(val matrix: List<List<Int>>, val baseScore: Int) {
    object I : TetrominoShape(listOf(listOf(1, 1, 1, 1)), 100)
    object O : TetrominoShape(listOf(listOf(1, 1), listOf(1, 1)), 80)
    object T : TetrominoShape(listOf(listOf(0, 1, 0), listOf(1, 1, 1)), 120)
    object S : TetrominoShape(listOf(listOf(0, 1, 1), listOf(1, 1, 0)), 90)
    object Z : TetrominoShape(listOf(listOf(1, 1, 0), listOf(0, 1, 1)), 90)
    object J : TetrominoShape(listOf(listOf(1, 0, 0), listOf(1, 1, 1)), 90)
    object L : TetrominoShape(listOf(listOf(0, 0, 1), listOf(1, 1, 1)), 90)

    companion object {
        val ALL = listOf(I, O, T, S, Z, J, L)
        fun random(): TetrominoShape = ALL.random()
        
        fun nameOf(shape: TetrominoShape): String {
            return when (shape) {
                is I -> "I-Block"
                is O -> "O-Block"
                is T -> "T-Block"
                is S -> "S-Block"
                is Z -> "Z-Block"
                is J -> "J-Block"
                is L -> "L-Block"
            }
        }
    }
}

enum class BlockElement(val icon: String, val color: Color, val label: String) {
    FIRE("🔥", Color(0xFFFF5252), "Fire"),
    WATER("💧", Color(0xFF40C4FF), "Water"),
    EARTH("🌿", Color(0xFF69F0AE), "Earth"),
    LIGHTNING("⚡", Color(0xFFFFD740), "Lightning")
}

data class CellState(
    val filled: Boolean = false,
    val color: Color = Color.Transparent,
    val element: BlockElement? = null,
    val level: Int = 1,
    val isCorner: Boolean = false
)

data class BlockPosition(var row: Int, var col: Int)

data class ActivePiece(
    val shape: TetrominoShape,
    val matrix: List<List<Int>>,
    var position: BlockPosition,
    val element: BlockElement = BlockElement.values().random(),
    val level: Int = 1
) {
    fun rotateClockwise(): List<List<Int>> {
        val r = matrix.size
        val c = matrix[0].size
        val temp = List(c) { MutableList(r) { 0 } }
        for (i in 0 until r) {
            for (j in 0 until c) {
                temp[j][r - 1 - i] = matrix[i][j]
            }
        }
        return temp
    }
}

data class JokerCard(
    val id: String,
    val name: String,
    val desc: String,
    val cost: Int,
    var active: Boolean = false,
    val icon: String = "🃏"
)

data class RelicItem(
    val id: String,
    val name: String,
    val desc: String,
    val cost: Int,
    val purchased: Boolean = false,
    val icon: String = "🔑"
)

data class Achievement(
    val id: String,
    val title: String,
    val desc: String,
    val progress: Int,
    val target: Int,
    val unlocked: Boolean = false,
    val icon: String = "🎯"
)

data class CosmeticSkin(
    val id: String,
    val name: String,
    val desc: String,
    val cost: Int,
    val color1: Color,
    val color2: Color,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false,
    val icon: String = "🎨"
)
