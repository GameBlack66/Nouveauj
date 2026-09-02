package com.example.game.model

import androidx.compose.ui.graphics.Color

enum class CarStyle {
    SPORTS,
    MUSCLE,
    CYBER,
    FORMULA,
    TRUCK
}

data class CarModel(
    val id: String,
    val name: String,
    val description: String,
    val style: CarStyle,
    val baseSpeed: Float, // km/h
    val baseAcceleration: Float,
    val baseHandling: Float,
    val baseNitroCapacity: Float,
    val price: Int,
    val defaultColor: Long,
    val accentColor: Long
)

enum class TrafficType {
    SEDAN,
    TRUCK,
    SPORTS,
    POLICE
}

data class TrafficVehicle(
    val id: Long,
    var x: Float, // -1f (left shoulder) to 1f (right shoulder)
    var y: Float, // 0f (horizon) to 1.1f (off screen bottom)
    val lane: Int,
    val speedRatio: Float,
    val type: TrafficType,
    val color: Long,
    val width: Float,
    val height: Float,
    var sirenFlash: Boolean = false
)

enum class CollectibleType {
    COIN,
    NITRO,
    REPAIR,
    MAGNET,
    SHIELD
}

data class Collectible(
    val id: Long,
    var x: Float,
    var y: Float,
    val type: CollectibleType,
    var collected: Boolean = false
)

data class VisualParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Long,
    var life: Float, // 1f down to 0f
    val maxLife: Float,
    val size: Float
)

data class SkidMark(
    val leftX: Float,
    val rightX: Float,
    val y: Float,
    var alpha: Float = 0.6f
)

enum class GameMode(val displayName: String, val description: String) {
    ENDLESS("Autoroute Trafic", "Survivez et esquivez le trafic à toute vitesse !"),
    TIME_ATTACK("Défi Chrono", "Franchissez les checkpoints avant la fin du compte à rebours !"),
    POLICE_CHASE("Poursuite", "Échappez aux barrages et patrouilles de police !")
}

val AVAILABLE_CARS = listOf(
    CarModel(
        id = "car_gt",
        name = "Apex GT",
        description = "Supercar équilibrée, idéale pour débuter sur l'asphalte.",
        style = CarStyle.SPORTS,
        baseSpeed = 180f,
        baseAcceleration = 0.7f,
        baseHandling = 0.8f,
        baseNitroCapacity = 100f,
        price = 0,
        defaultColor = 0xFFFF1E44, // Rouge racing
        accentColor = 0xFFFFFFFF
    ),
    CarModel(
        id = "car_muscle",
        name = "Thunder V8",
        description = "Monstre américain avec un couple brutal et une vitesse de pointe infernale.",
        style = CarStyle.MUSCLE,
        baseSpeed = 205f,
        baseAcceleration = 0.85f,
        baseHandling = 0.65f,
        baseNitroCapacity = 110f,
        price = 450,
        defaultColor = 0xFFFF8500, // Orange feu
        accentColor = 0xFF111111
    ),
    CarModel(
        id = "car_cyber",
        name = "Neon Phantom",
        description = "Châssis cyberpunk futuriste équipé d'un propulseur nitro ionique.",
        style = CarStyle.CYBER,
        baseSpeed = 220f,
        baseAcceleration = 0.9f,
        baseHandling = 0.85f,
        baseNitroCapacity = 140f,
        price = 1200,
        defaultColor = 0xFF00F0FF, // Cyan néon
        accentColor = 0xFFFF0055
    ),
    CarModel(
        id = "car_formula",
        name = "Formula Apex",
        description = "Monoplace de circuit offrant une agilité chirurgicale et une reprise folle.",
        style = CarStyle.FORMULA,
        baseSpeed = 240f,
        baseAcceleration = 1.0f,
        baseHandling = 1.0f,
        baseNitroCapacity = 90f,
        price = 2500,
        defaultColor = 0xFF39FF14, // Vert fluo
        accentColor = 0xFF0A0E17
    ),
    CarModel(
        id = "car_beast",
        name = "Goliath Beast",
        description = "Véhicule tout-terrain ultra blindé réduisant les dégâts des chocs.",
        style = CarStyle.TRUCK,
        baseSpeed = 175f,
        baseAcceleration = 0.75f,
        baseHandling = 0.6f,
        baseNitroCapacity = 120f,
        price = 1800,
        defaultColor = 0xFF8A2BE2, // Violet blindé
        accentColor = 0xFFFFD700
    )
)

val CAR_PAINTS = listOf(
    0xFFFF1E44, // Rouge Flash
    0xFF00F0FF, // Cyan Néon
    0xFFFF8500, // Orange Flamme
    0xFF39FF14, // Vert Vipère
    0xFFFFD700, // Or Chaud
    0xFF9D00FF, // Violet Cyber
    0xFF1F2430, // Noir Carbone
    0xFF0077FE  // Bleu Électrique
)
