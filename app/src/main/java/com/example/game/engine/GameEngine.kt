package com.example.game.engine

import com.example.game.audio.GameAudio
import com.example.game.model.*
import com.example.game.repository.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class GameStateSnapshot(
    val score: Int = 0,
    val distanceMeters: Float = 0f,
    val speedKmh: Float = 0f,
    val playerX: Float = 0f,
    val playerY: Float = 0.78f,
    val steerAngle: Float = 0f, // in degrees, for visual tilt
    val health: Float = 100f,
    val nitro: Float = 100f,
    val maxNitro: Float = 100f,
    val coins: Int = 0,
    val isNitroActive: Boolean = false,
    val isBraking: Boolean = false,
    val isInvulnerable: Boolean = false,
    val isShieldActive: Boolean = false,
    val isMagnetActive: Boolean = false,
    val nearMissCombo: Int = 0,
    val nearMissToast: String? = null,
    val timeRemaining: Float = 45f,
    val checkpointDistance: Float = 1000f,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val isNewHighScore: Boolean = false,
    val roadOffset: Float = 0f,
    val traffic: List<TrafficVehicle> = emptyList(),
    val collectibles: List<Collectible> = emptyList(),
    val particles: List<VisualParticle> = emptyList(),
    val skidMarks: List<SkidMark> = emptyList(),
    val screenShake: Float = 0f,
    val damageFlash: Boolean = false
)

class GameEngine(
    private val preferences: GamePreferences,
    private val audio: GameAudio,
    val mode: GameMode,
    val car: CarModel
) {
    private val _state = MutableStateFlow(GameStateSnapshot())
    val state: StateFlow<GameStateSnapshot> = _state.asStateFlow()

    // Upgrades calculation
    private val engineLevel = preferences.getUpgradeLevel(car.id, "engine")
    private val handlingLevel = preferences.getUpgradeLevel(car.id, "handling")
    private val nitroLevel = preferences.getUpgradeLevel(car.id, "nitro")

    private val maxSpeed = car.baseSpeed + (engineLevel * 12f)
    private val accelerationRate = car.baseAcceleration + (engineLevel * 0.08f)
    private val steerSpeed = 2.2f + (handlingLevel * 0.35f)
    private val maxNitroCapacity = car.baseNitroCapacity + (nitroLevel * 15f)

    private var currentSpeed = 0f
    private var playerX = 0f
    private var health = 100f
    private var nitro = maxNitroCapacity
    private var coinsInRun = 0
    private var distance = 0f
    private var score = 0
    private var roadOffset = 0f

    private var invulnerableTimer = 0f
    private var shieldTimer = 0f
    private var magnetTimer = 0f
    private var nearMissToastTimer = 0f
    private var nearMissToastText: String? = null
    private var nearMissCombo = 0
    private var comboResetTimer = 0f
    private var timeAttackRemaining = 50f
    private var nextCheckpoint = 800f

    private var nextTrafficSpawnDistance = 30f
    private var nextCollectibleSpawnDistance = 60f

    private val trafficList = mutableListOf<TrafficVehicle>()
    private val collectibleList = mutableListOf<Collectible>()
    private val particleList = mutableListOf<VisualParticle>()
    private val skidMarkList = mutableListOf<SkidMark>()

    private var currentSteerInput = 0f // -1 to +1
    private var isNitroRequested = false
    private var isBrakeRequested = false
    private var screenShakeIntensity = 0f
    private var nextVehicleId = 1L
    private var nextCollectibleId = 1L

    init {
        _state.update {
            it.copy(
                maxNitro = maxNitroCapacity,
                nitro = maxNitroCapacity,
                timeRemaining = timeAttackRemaining
            )
        }
    }

    fun setSteerInput(steer: Float) {
        currentSteerInput = steer.coerceIn(-1f, 1f)
    }

    fun setDirectPlayerX(targetX: Float) {
        // Direct finger tracking
        val clamped = targetX.coerceIn(-0.85f, 0.85f)
        val diff = clamped - playerX
        currentSteerInput = (diff * 4f).coerceIn(-1f, 1f)
    }

    fun setNitro(active: Boolean) {
        isNitroRequested = active
    }

    fun setBrake(active: Boolean) {
        isBrakeRequested = active
    }

    fun togglePause() {
        _state.update { it.copy(isPaused = !it.isPaused) }
    }

    fun update(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)
        val currentState = _state.value
        if (currentState.isGameOver || currentState.isPaused) return

        // 1. Calculate Speeds & Nitro
        val isNitroActive = isNitroRequested && nitro > 0
        if (isNitroActive) {
            nitro = max(0f, nitro - (22f * dt))
            screenShakeIntensity = max(screenShakeIntensity, 3.5f)
            audio.playNitro()
        } else if (!isNitroRequested && nitro < maxNitroCapacity) {
            // Passive recharge
            nitro = min(maxNitroCapacity, nitro + (4f * dt))
        }

        val targetSpeed = when {
            isNitroActive -> maxSpeed + 45f
            isBrakeRequested -> 65f
            else -> maxSpeed
        }

        val accelFactor = if (isBrakeRequested) 4.5f else if (isNitroActive) 3.5f else 1.8f
        currentSpeed += (targetSpeed - currentSpeed) * (accelerationRate * accelFactor * dt)
        currentSpeed = currentSpeed.coerceIn(0f, maxSpeed + 55f)

        // 2. Lateral Movement
        val actualSteer = currentSteerInput
        playerX += actualSteer * steerSpeed * dt
        playerX = playerX.coerceIn(-0.82f, 0.82f)

        // Visual tilt angle
        val targetTilt = actualSteer * 14f
        val currentTilt = currentState.steerAngle + (targetTilt - currentState.steerAngle) * (15f * dt)

        // 3. Distance & Road Offset
        val metersThisFrame = (currentSpeed * 1000f / 3600f) * dt
        distance += metersThisFrame
        roadOffset = (roadOffset + (currentSpeed * 0.08f * dt)) % 1.0f

        // Score based on speed & distance
        val speedBonus = (currentSpeed / 100f)
        score += (metersThisFrame * speedBonus * (1 + nearMissCombo * 0.25f)).toInt()

        // 4. Mode Specific: Time Attack
        if (mode == GameMode.TIME_ATTACK) {
            timeAttackRemaining -= dt
            if (distance >= nextCheckpoint) {
                timeAttackRemaining += 15f
                nextCheckpoint += 1000f
                coinsInRun += 15
                audio.playUpgrade()
                nearMissToastText = "CHECKPOINT ! +15s"
                nearMissToastTimer = 2.0f
            }
            if (timeAttackRemaining <= 0f) {
                timeAttackRemaining = 0f
                triggerGameOver()
                return
            }
        }

        // 5. Timers
        if (invulnerableTimer > 0f) invulnerableTimer -= dt
        if (shieldTimer > 0f) shieldTimer -= dt
        if (magnetTimer > 0f) magnetTimer -= dt
        if (nearMissToastTimer > 0f) {
            nearMissToastTimer -= dt
            if (nearMissToastTimer <= 0f) nearMissToastText = null
        }
        if (comboResetTimer > 0f) {
            comboResetTimer -= dt
            if (comboResetTimer <= 0f) nearMissCombo = 0
        }
        if (screenShakeIntensity > 0f) {
            screenShakeIntensity = max(0f, screenShakeIntensity - 12f * dt)
        }

        // 6. Spawn Traffic & Collectibles
        nextTrafficSpawnDistance -= metersThisFrame
        if (nextTrafficSpawnDistance <= 0f) {
            spawnTrafficVehicle()
            // Random distance to next car: decreases as player drives further
            val minGap = max(18f, 40f - (distance / 500f))
            nextTrafficSpawnDistance = minGap + Random.nextFloat() * 25f
        }

        nextCollectibleSpawnDistance -= metersThisFrame
        if (nextCollectibleSpawnDistance <= 0f) {
            spawnCollectible()
            nextCollectibleSpawnDistance = 45f + Random.nextFloat() * 50f
        }

        // 7. Update Traffic Vehicles
        val playerRelY = 0.78f
        val iterator = trafficList.iterator()
        while (iterator.hasNext()) {
            val tv = iterator.next()
            // Relative speed between player and traffic car
            // Speed of road = playerSpeed; speed of traffic = playerSpeed * ratio
            val trafficRelativeSpeed = currentSpeed - (maxSpeed * tv.speedRatio)
            val moveDown = (trafficRelativeSpeed * 0.0035f) * dt
            tv.y += moveDown

            // Police flashing siren
            if (tv.type == TrafficType.POLICE) {
                tv.sirenFlash = ((System.currentTimeMillis() / 150) % 2) == 0L
            }

            // Check near-miss before checking collision
            val dy = abs(tv.y - playerRelY)
            val dx = abs(tv.x - playerX)

            if (dy < 0.08f && dx in 0.16f..0.34f && trafficRelativeSpeed > 25f) {
                // Near miss!
                if (!tv.sirenFlash) { // use as a flag so it triggers once per vehicle
                    onNearMiss()
                    tv.sirenFlash = true
                }
            }

            // Collision Check
            val collisionXRange = (tv.width * 0.5f) + 0.10f
            val collisionYRange = 0.09f
            if (dy < collisionYRange && dx < collisionXRange) {
                handleCollision(tv)
                iterator.remove()
                continue
            }

            // Remove off-screen traffic
            if (tv.y > 1.25f || tv.y < -0.35f) {
                iterator.remove()
            }
        }

        // 8. Update Collectibles
        val collIterator = collectibleList.iterator()
        while (collIterator.hasNext()) {
            val item = collIterator.next()
            // Move with road
            item.y += (currentSpeed * 0.0035f) * dt

            // Magnet pulling coins
            if (magnetTimer > 0f && item.type == CollectibleType.COIN) {
                val dx = playerX - item.x
                val dy = playerRelY - item.y
                item.x += dx * 6f * dt
                item.y += dy * 6f * dt
            }

            // Check pickup
            val distToPlayer = kotlin.math.hypot(item.x - playerX, item.y - playerRelY)
            if (distToPlayer < 0.14f && !item.collected) {
                item.collected = true
                onCollectItem(item)
                collIterator.remove()
                continue
            }

            if (item.y > 1.2f) {
                collIterator.remove()
            }
        }

        // 9. Particle Systems
        updateParticles(dt, isNitroActive, actualSteer)

        // 10. Update Skid Marks
        if (abs(actualSteer) > 0.55f || isBrakeRequested) {
            skidMarkList.add(
                SkidMark(
                    leftX = playerX - 0.08f,
                    rightX = playerX + 0.08f,
                    y = playerRelY + 0.05f,
                    alpha = 0.5f
                )
            )
        }
        val skidIterator = skidMarkList.iterator()
        while (skidIterator.hasNext()) {
            val sm = skidIterator.next()
            sm.alpha -= 1.2f * dt
            if (sm.alpha <= 0f) {
                skidIterator.remove()
            }
        }
        if (skidMarkList.size > 50) {
            skidMarkList.removeAt(0)
        }

        // Check game over
        val isDead = health <= 0f
        if (isDead) {
            triggerGameOver()
            return
        }

        // Emit snapshot
        _state.update {
            it.copy(
                score = score,
                distanceMeters = distance,
                speedKmh = currentSpeed,
                playerX = playerX,
                playerY = playerRelY,
                steerAngle = currentTilt,
                health = health,
                nitro = nitro,
                coins = coinsInRun,
                isNitroActive = isNitroActive,
                isBraking = isBrakeRequested,
                isInvulnerable = invulnerableTimer > 0f || shieldTimer > 0f,
                isShieldActive = shieldTimer > 0f,
                isMagnetActive = magnetTimer > 0f,
                nearMissCombo = nearMissCombo,
                nearMissToast = nearMissToastText,
                timeRemaining = timeAttackRemaining,
                roadOffset = roadOffset,
                traffic = trafficList.toList(),
                collectibles = collectibleList.toList(),
                particles = particleList.toList(),
                skidMarks = skidMarkList.toList(),
                screenShake = screenShakeIntensity,
                damageFlash = invulnerableTimer > 0.9f
            )
        }
    }

    private fun spawnTrafficVehicle() {
        val lanes = listOf(-0.52f, 0f, 0.52f)
        val selectedLaneIdx = Random.nextInt(lanes.size)
        val laneX = lanes[selectedLaneIdx]

        // Ensure no other car is currently near horizon on that lane
        val tooClose = trafficList.any { it.lane == selectedLaneIdx && it.y < 0.15f }
        if (tooClose) return

        val type = when (mode) {
            GameMode.POLICE_CHASE -> if (Random.nextFloat() < 0.45f) TrafficType.POLICE else TrafficType.SEDAN
            else -> {
                val r = Random.nextFloat()
                when {
                    r < 0.50f -> TrafficType.SEDAN
                    r < 0.75f -> TrafficType.TRUCK
                    r < 0.92f -> TrafficType.SPORTS
                    else -> TrafficType.POLICE
                }
            }
        }

        val color = when (type) {
            TrafficType.SEDAN -> listOf(0xFF2C3E50, 0xFF7F8C8D, 0xFFE74C3C, 0xFF3498DB).random()
            TrafficType.TRUCK -> listOf(0xFF8B4513, 0xFF2F4F4F, 0xFF1C2833).random()
            TrafficType.SPORTS -> listOf(0xFFFFD700, 0xFFFF1493, 0xFF00FF7F).random()
            TrafficType.POLICE -> 0xFF111111
        }

        val width = if (type == TrafficType.TRUCK) 0.24f else 0.18f
        val height = if (type == TrafficType.TRUCK) 0.22f else 0.16f
        val speedRatio = when (type) {
            TrafficType.TRUCK -> 0.45f
            TrafficType.SEDAN -> 0.58f
            TrafficType.SPORTS -> 0.78f
            TrafficType.POLICE -> 0.82f
        }

        trafficList.add(
            TrafficVehicle(
                id = nextVehicleId++,
                x = laneX,
                y = -0.1f,
                lane = selectedLaneIdx,
                speedRatio = speedRatio,
                type = type,
                color = color,
                width = width,
                height = height
            )
        )
    }

    private fun spawnCollectible() {
        val lanes = listOf(-0.52f, 0f, 0.52f)
        val laneX = lanes.random()

        val r = Random.nextFloat()
        val type = when {
            r < 0.65f -> CollectibleType.COIN
            r < 0.82f -> CollectibleType.NITRO
            r < 0.90f -> CollectibleType.REPAIR
            r < 0.96f -> CollectibleType.MAGNET
            else -> CollectibleType.SHIELD
        }

        collectibleList.add(
            Collectible(
                id = nextCollectibleId++,
                x = laneX,
                y = -0.08f,
                type = type
            )
        )
    }

    private fun handleCollision(tv: TrafficVehicle) {
        if (shieldTimer > 0f) {
            // Player has shield: knock out traffic vehicle with sparks!
            spawnCrashParticles(tv.x, tv.y, 25)
            score += 200
            audio.playCrash()
            return
        }

        if (invulnerableTimer > 0f) return

        // Take damage
        val damage = when {
            car.style == CarStyle.TRUCK -> 18f
            tv.type == TrafficType.TRUCK -> 38f
            else -> 28f
        }

        health = max(0f, health - damage)
        currentSpeed = max(40f, currentSpeed * 0.55f)
        screenShakeIntensity = 14f
        invulnerableTimer = 1.3f
        nearMissCombo = 0
        audio.playCrash()
        spawnCrashParticles(playerX, 0.78f, 20)
    }

    private fun onNearMiss() {
        nearMissCombo++
        comboResetTimer = 3.5f
        val points = 100 * nearMissCombo
        score += points
        nitro = min(maxNitroCapacity, nitro + 12f)
        nearMissToastText = if (nearMissCombo > 1) "FRÔLEMENT x$nearMissCombo ! +$points" else "FRÔLEMENT ! +100"
        nearMissToastTimer = 1.5f
        audio.playNearMiss()
    }

    private fun onCollectItem(item: Collectible) {
        when (item.type) {
            CollectibleType.COIN -> {
                coinsInRun++
                score += 50
                audio.playCoin()
                spawnSparks(item.x, item.y, 0xFFFFD700, 8)
            }
            CollectibleType.NITRO -> {
                nitro = min(maxNitroCapacity, nitro + 40f)
                audio.playUpgrade()
                spawnSparks(item.x, item.y, 0xFF00F0FF, 10)
                nearMissToastText = "NITRO RECHARGE !"
                nearMissToastTimer = 1.2f
            }
            CollectibleType.REPAIR -> {
                health = min(100f, health + 30f)
                audio.playUpgrade()
                spawnSparks(item.x, item.y, 0xFF00FF7F, 10)
                nearMissToastText = "RÉPARATION +30% !"
                nearMissToastTimer = 1.2f
            }
            CollectibleType.MAGNET -> {
                magnetTimer = 9.0f
                audio.playUpgrade()
                spawnSparks(item.x, item.y, 0xFFFF00FF, 12)
                nearMissToastText = "AIMANT À PIÈCES !"
                nearMissToastTimer = 1.5f
            }
            CollectibleType.SHIELD -> {
                shieldTimer = 7.0f
                audio.playUpgrade()
                spawnSparks(item.x, item.y, 0xFFFFFF00, 15)
                nearMissToastText = "BOUCLIER ACTIF !"
                nearMissToastTimer = 1.5f
            }
        }
    }

    private fun updateParticles(dt: Float, isNitroActive: Boolean, steer: Float) {
        // Nitro flame particles
        if (isNitroActive) {
            repeat(3) {
                val pX = playerX + (Random.nextFloat() - 0.5f) * 0.08f
                val pY = 0.86f + Random.nextFloat() * 0.04f
                val color = if (Random.nextBoolean()) 0xFF00F0FF else 0xFFFF8500
                particleList.add(
                    VisualParticle(
                        x = pX,
                        y = pY,
                        vx = (Random.nextFloat() - 0.5f) * 0.3f,
                        vy = 0.6f + Random.nextFloat() * 0.8f,
                        color = color,
                        life = 1f,
                        maxLife = 0.25f,
                        size = 7f + Random.nextFloat() * 9f
                    )
                )
            }
        }

        // Drift smoke particles
        if (abs(steer) > 0.6f) {
            val side = if (steer > 0) -1 else 1
            particleList.add(
                VisualParticle(
                    x = playerX + (side * 0.09f),
                    y = 0.85f,
                    vx = (side * -0.4f) + (Random.nextFloat() - 0.5f) * 0.2f,
                    vy = 0.4f,
                    color = 0xAAFFFFFF,
                    life = 1f,
                    maxLife = 0.35f,
                    size = 10f + Random.nextFloat() * 12f
                )
            )
        }

        // Particle update & decay
        val pIter = particleList.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt / p.maxLife
            p.x += p.vx * dt
            p.y += p.vy * dt
            if (p.life <= 0f) {
                pIter.remove()
            }
        }
    }

    private fun spawnCrashParticles(x: Float, y: Float, count: Int) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = 0.5f + Random.nextFloat() * 1.5f
            val colors = listOf(0xFFFF1E44, 0xFFFF8500, 0xFFFFD700, 0xFF444444)
            particleList.add(
                VisualParticle(
                    x = x,
                    y = y,
                    vx = (kotlin.math.cos(angle) * speed).toFloat(),
                    vy = (kotlin.math.sin(angle) * speed).toFloat(),
                    color = colors.random(),
                    life = 1f,
                    maxLife = 0.55f,
                    size = 8f + Random.nextFloat() * 12f
                )
            )
        }
    }

    private fun spawnSparks(x: Float, y: Float, color: Long, count: Int) {
        repeat(count) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = 0.3f + Random.nextFloat() * 0.8f
            particleList.add(
                VisualParticle(
                    x = x,
                    y = y,
                    vx = (kotlin.math.cos(angle) * speed).toFloat(),
                    vy = (kotlin.math.sin(angle) * speed).toFloat(),
                    color = color,
                    life = 1f,
                    maxLife = 0.35f,
                    size = 5f + Random.nextFloat() * 6f
                )
            )
        }
    }

    private fun triggerGameOver() {
        val currentHighScore = preferences.getHighScore(mode.name)
        val isNew = score > currentHighScore

        preferences.addCoins(coinsInRun)
        preferences.setHighScore(mode.name, score)
        preferences.setBestDistance(mode.name, distance)

        _state.update {
            it.copy(
                isGameOver = true,
                isNewHighScore = isNew
            )
        }
    }
}
