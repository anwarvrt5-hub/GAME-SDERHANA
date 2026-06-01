package com.example.game

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TetratroPrefs", Context.MODE_PRIVATE)

    fun getHighScore(modeId: String): Int {
        return prefs.getInt("high_score_$modeId", 0)
    }

    fun saveHighScore(modeId: String, score: Int) {
        val current = getHighScore(modeId)
        if (score > current) {
            prefs.edit().putInt("high_score_$modeId", score).apply()
        }
    }

    var globalCoins: Int
        get() = prefs.getInt("global_coins", 150)
        set(value) = prefs.edit().putInt("global_coins", value).apply()

    // Statistics
    var totalGamesPlayed: Int
        get() = prefs.getInt("stat_games_played", 0)
        set(value) = prefs.edit().putInt("stat_games_played", value).apply()

    var totalLinesCleared: Int
        get() = prefs.getInt("stat_lines_cleared", 0)
        set(value) = prefs.edit().putInt("stat_lines_cleared", value).apply()

    var maxMultiplier: Int
        get() = prefs.getInt("stat_max_multi", 1)
        set(value) = prefs.edit().putInt("stat_max_multi", value).apply()

    var totalMonstersDefeated: Int
        get() = prefs.getInt("stat_monsters_defeated", 0)
        set(value) = prefs.edit().putInt("stat_monsters_defeated", value).apply()

    var totalGamblingWins: Int
        get() = prefs.getInt("stat_gambling_wins", 0)
        set(value) = prefs.edit().putInt("stat_gambling_wins", value).apply()

    var totalFusionsMerged: Int
        get() = prefs.getInt("stat_fusions_merged", 0)
        set(value) = prefs.edit().putInt("stat_fusions_merged", value).apply()

    var totalFactoryCoinsEarned: Int
        get() = prefs.getInt("stat_factory_coins_earned", 0)
        set(value) = prefs.edit().putInt("stat_factory_coins_earned", value).apply()

    // Achievements Config
    fun isAchievementUnlocked(id: String): Boolean {
        return prefs.getBoolean("achievement_unlocked_$id", false)
    }

    fun setAchievementUnlocked(id: String, unlocked: Boolean) {
        prefs.edit().putBoolean("achievement_unlocked_$id", unlocked).apply()
    }

    fun getAchievementProgress(id: String): Int {
        return prefs.getInt("achievement_prog_$id", 0)
    }

    fun saveAchievementProgress(id: String, progress: Int) {
        prefs.edit().putInt("achievement_prog_$id", progress).apply()
    }

    // Unlocks Collection
    fun isItemUnlocked(id: String): Boolean {
        // Some defaults are unlocked
        if (id == "joker_t" || id == "skin_classic" || id == "item_feather") return true
        return prefs.getBoolean("unlocked_item_$id", false)
    }

    fun setItemUnlocked(id: String, unlocked: Boolean) {
        prefs.edit().putBoolean("unlocked_item_$id", unlocked).apply()
    }

    // Current Equipped Skin identifier
    var equippedSkinId: String
        get() = prefs.getString("equipped_skin_id", "skin_classic") ?: "skin_classic"
        set(value) = prefs.edit().putString("equipped_skin_id", value).apply()

    fun resetAll() {
        prefs.edit().apply {
            clear()
            putInt("global_coins", 150)
            apply()
        }
    }
}
