package com.example.game.repository

import android.content.Context
import android.content.SharedPreferences

enum class ControlType {
    TOUCH_BUTTONS,
    TOUCH_DRAG
}

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_racer_prefs", Context.MODE_PRIVATE)

    var totalCoins: Int
        get() = prefs.getInt("total_coins", 150)
        set(value) = prefs.edit().putInt("total_coins", value).apply()

    fun addCoins(amount: Int) {
        totalCoins += amount
    }

    fun spendCoins(amount: Int): Boolean {
        return if (totalCoins >= amount) {
            totalCoins -= amount
            true
        } else false
    }

    var selectedCarId: String
        get() = prefs.getString("selected_car_id", "car_gt") ?: "car_gt"
        set(value) = prefs.edit().putString("selected_car_id", value).apply()

    fun isCarUnlocked(carId: String): Boolean {
        if (carId == "car_gt") return true
        return prefs.getBoolean("car_unlocked_$carId", false)
    }

    fun unlockCar(carId: String) {
        prefs.edit().putBoolean("car_unlocked_$carId", true).apply()
    }

    fun getCarColor(carId: String, defaultColor: Long): Long {
        return prefs.getLong("car_color_$carId", defaultColor)
    }

    fun setCarColor(carId: String, color: Long) {
        prefs.edit().putLong("car_color_$carId", color).apply()
    }

    fun getUpgradeLevel(carId: String, upgradeType: String): Int {
        return prefs.getInt("upgrade_${carId}_$upgradeType", 0)
    }

    fun incrementUpgrade(carId: String, upgradeType: String) {
        val current = getUpgradeLevel(carId, upgradeType)
        prefs.edit().putInt("upgrade_${carId}_$upgradeType", current + 1).apply()
    }

    fun getHighScore(mode: String): Int {
        return prefs.getInt("high_score_$mode", 0)
    }

    fun setHighScore(mode: String, score: Int) {
        val current = getHighScore(mode)
        if (score > current) {
            prefs.edit().putInt("high_score_$mode", score).apply()
        }
    }

    fun getBestDistance(mode: String): Float {
        return prefs.getFloat("best_distance_$mode", 0f)
    }

    fun setBestDistance(mode: String, distance: Float) {
        val current = getBestDistance(mode)
        if (distance > current) {
            prefs.edit().putFloat("best_distance_$mode", distance).apply()
        }
    }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var controlType: ControlType
        get() {
            val name = prefs.getString("control_type", ControlType.TOUCH_BUTTONS.name)
            return try {
                ControlType.valueOf(name ?: ControlType.TOUCH_BUTTONS.name)
            } catch (_: Exception) {
                ControlType.TOUCH_BUTTONS
            }
        }
        set(value) = prefs.edit().putString("control_type", value.name).apply()
}
