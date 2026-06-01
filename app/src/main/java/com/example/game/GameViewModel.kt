package com.example.game

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GameViewModel(context: Context) : ViewModel() {
    private val prefs = GamePreferences(context)

    // Grid details
    val rows = 20
    val cols = 10

    // Board grid representation
    private val _grid = MutableStateFlow(Array(rows) { Array(cols) { CellState() } })
    val grid: StateFlow<Array<Array<CellState>>> = _grid.asStateFlow()

    // Active block details
    private val _activePiece = MutableStateFlow<ActivePiece?>(null)
    val activePiece: StateFlow<ActivePiece?> = _activePiece.asStateFlow()

    private val _nextPiece = MutableStateFlow<TetrominoShape>(TetrominoShape.random())
    val nextPiece: StateFlow<TetrominoShape> = _nextPiece.asStateFlow()

    private val _holdPiece = MutableStateFlow<TetrominoShape?>(null)
    val holdPiece: StateFlow<TetrominoShape?> = _holdPiece.asStateFlow()

    private var hasHeldThisTurn = false

    // General Score & Multiplier details
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _multiplier = MutableStateFlow(1f)
    val multiplier: StateFlow<Float> = _multiplier.asStateFlow()

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _targetScore = MutableStateFlow(500)
    val targetScore: StateFlow<Int> = _targetScore.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _isWon = MutableStateFlow(false)
    val isWon: StateFlow<Boolean> = _isWon.asStateFlow()

    private val _currentGameMode = MutableStateFlow(GameMode.TETRATRO)
    val currentGameMode: StateFlow<GameMode> = _currentGameMode.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<String>>(listOf("Welcome to TETRATRO!", "Choose a mode and beat the threshold."))
    val eventLogs: StateFlow<List<String>> = _eventLogs.asStateFlow()

    // Mode Specific fields
    // Mode 1: Tetratro Jokers
    private val _jokers = MutableStateFlow<List<JokerCard>>(emptyList())
    val jokers: StateFlow<List<JokerCard>> = _jokers.asStateFlow()

    // Mode 2: Elementris
    private val _shieldActive = MutableStateFlow(false)
    val shieldActive: StateFlow<Boolean> = _shieldActive.asStateFlow()

    // Mode 3: Combo Stacker
    private val _relics = MutableStateFlow<List<RelicItem>>(emptyList())
    val relics: StateFlow<List<RelicItem>> = _relics.asStateFlow()
    private var comboChain = 0

    // Mode 4: Dungeon Tetris RPG
    private val _monsterHp = MutableStateFlow(200)
    val monsterHp: StateFlow<Int> = _monsterHp.asStateFlow()

    private val _monsterMaxHp = MutableStateFlow(200)
    val monsterMaxHp: StateFlow<Int> = _monsterMaxHp.asStateFlow()

    private val _monsterName = MutableStateFlow("Green Slime")
    val monsterName: StateFlow<String> = _monsterName.asStateFlow()

    private val _playerHp = MutableStateFlow(100)
    val playerHp: StateFlow<Int> = _playerHp.asStateFlow()

    // Mode 5: Tetris Casino
    private val _casinoHighRiskMode = MutableStateFlow(false)
    val casinoHighRiskMode: StateFlow<Boolean> = _casinoHighRiskMode.asStateFlow()

    // Mode 6: Factory Automation
    private val _factoryCredits = MutableStateFlow(0f)
    val factoryCredits: StateFlow<Float> = _factoryCredits.asStateFlow()
    private val _minerCount = MutableStateFlow(0)
    val minerCount: StateFlow<Int> = _minerCount.asStateFlow()
    private val _sweeperCount = MutableStateFlow(0)
    val sweeperCount: StateFlow<Int> = _sweeperCount.asStateFlow()

    // State of skins & settings
    val activeSkinId = mutableStateOf(prefs.equippedSkinId)

    // Game loop control
    private var gameJob: Job? = null
    private var autoTickDelay = 900L // speed in ms

    init {
        loadSetupData()
    }

    private fun loadSetupData() {
        // Initialize customizable items
        activeSkinId.value = prefs.equippedSkinId
    }

    fun startNewGame(mode: GameMode) {
        _currentGameMode.value = mode
        _score.value = 0
        _multiplier.value = 1f
        _coins.value = 0
        _level.value = 1
        _targetScore.value = when (mode) {
            GameMode.TETRATRO -> 500
            GameMode.TETRIS_CASINO -> 750
            GameMode.FUSION_TETRIS -> 1000
            else -> 600
        }
        _isGameOver.value = false
        _isWon.value = false
        _holdPiece.value = null
        hasHeldThisTurn = false
        comboChain = 0
        _shieldActive.value = false
        _playerHp.value = 100

        // Reset Factory
        _factoryCredits.value = 0f
        _minerCount.value = 0
        _sweeperCount.value = 0

        // Reset Dungeon Monster
        _monsterName.value = "Green Slime"
        _monsterHp.value = 200
        _monsterMaxHp.value = 200

        // Clear grid
        _grid.value = Array(rows) { Array(cols) { CellState() } }

        // Setup mode-specific Jokers/Relics
        if (mode == GameMode.TETRATRO) {
            _jokers.value = listOf(
                JokerCard("joker_t", "T-Joker", "T-shapes get +50% multiplier", 80, active = true, icon = "🃏"),
                JokerCard("joker_quad", "Quad Streaker", "Clearing 4 lines gives x3 multiplier", 110, active = false, icon = "⚡"),
                JokerCard("joker_corner", "Corner Booster", "Landed blocks touching corners get +100 bonus", 90, active = false, icon = "🎯"),
                JokerCard("joker_foil", "Foil Card", "+200 flat points on any line clears", 130, active = false, icon = "✨")
            )
        } else if (mode == GameMode.COMBO_STACKER) {
            _relics.value = listOf(
                RelicItem("relic_feather", "Golden Feather", "Slowing falling speed down by 20%", 50, purchased = false, icon = "🪶"),
                RelicItem("relic_key", "Shape Alter", "Reduce Z/S shape spawn chance by 50%", 90, purchased = false, icon = "🔑"),
                RelicItem("relic_shield", "Shield Charm", "Auto clears bottom 3 lines when grid top level reaches 15", 150, purchased = false, icon = "🛡️")
            )
        }

        addLog("Starting ${mode.title} round!")
        addLog(mode.desc)

        // Spawn first piece
        spawnPiece()

        // Start tick loop coroutine
        gameJob?.cancel()
        gameJob = viewModelScope.launch(Dispatchers.Default) {
            var lastFactoryTick = System.currentTimeMillis()
            autoTickDelay = if (mode == GameMode.COMBO_STACKER) 1000L else 900L

            while (isActive && !_isGameOver.value && !_isWon.value) {
                val speedModifier = if (mode == GameMode.COMBO_STACKER && _relics.value.firstOrNull { it.id == "relic_feather" }?.purchased == true) 1.25f else 1.0f
                val delayTime = (autoTickDelay * speedModifier).toLong().coerceAtLeast(150)
                delay(delayTime)

                // Factory resources tick
                val elapsed = System.currentTimeMillis() - lastFactoryTick
                if (elapsed >= 1000 && _currentGameMode.value == GameMode.FACTORY_BLOCKS) {
                    lastFactoryTick = System.currentTimeMillis()
                    // Passive minerals + score
                    val placedCount = countPlacedBlocks()
                    val creditsGot = placedCount * 0.1f + (_minerCount.value * 2.5f)
                    _factoryCredits.value += creditsGot
                    _score.value += (_minerCount.value * 5)
                    if (_minerCount.value > 0) {
                        prefs.totalFactoryCoinsEarned += (_minerCount.value * 5)
                    }

                    // Auto-Sweepers clearing bottom rows
                    if (_sweeperCount.value > 0 && Math.random() < 0.15) {
                        clearBottomRow()
                        addLog("🏭 Auto-Sweeper cleared a bottom row!")
                    }
                }

                // Game Loop tick
                withContext(Dispatchers.Main) {
                    if (!_isGameOver.value && !_isWon.value) {
                        tick()
                    }
                }
            }
        }

        prefs.totalGamesPlayed += 1
    }

    private fun countPlacedBlocks(): Int {
        var cnt = 0
        val board = _grid.value
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].filled) cnt++
            }
        }
        return cnt
    }

    private fun countTopBlockRow(): Int {
        val board = _grid.value
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].filled) return r
            }
        }
        return rows
    }

    private fun clearBottomRow() {
        val board = _grid.value
        val newBoard = Array(rows) { Array(cols) { CellState() } }
        // shift down everything from row 0 to 18
        for (r in 18 downTo 0) {
            for (c in 0 until cols) {
                newBoard[r + 1][c] = board[r][c]
            }
        }
        _grid.value = newBoard
    }

    private fun addLog(message: String) {
        val current = _eventLogs.value.toMutableList()
        current.add(0, message)
        if (current.size > 12) {
            current.removeAt(current.size - 1)
        }
        _eventLogs.value = current
    }

    private fun spawnPiece() {
        hasHeldThisTurn = false
        val shape = _nextPiece.value

        // Setup random modifier shape based on selection relics
        var nextShapeChoice = TetrominoShape.random()
        if (_currentGameMode.value == GameMode.COMBO_STACKER) {
            val keyRelic = _relics.value.firstOrNull { it.id == "relic_key" }
            if (keyRelic?.purchased == true) {
                // reduce probability of S and Z
                if (nextShapeChoice == TetrominoShape.S || nextShapeChoice == TetrominoShape.Z) {
                    if (Math.random() < 0.5) {
                        nextShapeChoice = listOf(TetrominoShape.I, TetrominoShape.O, TetrominoShape.T, TetrominoShape.L, TetrominoShape.J).random()
                    }
                }
            }
        }

        _nextPiece.value = nextShapeChoice

        val element = BlockElement.values().random()
        val matrix = shape.matrix
        val pos = BlockPosition(0, 5 - (matrix[0].size / 2))

        val piece = ActivePiece(
            shape = shape,
            matrix = matrix,
            position = pos,
            element = element,
            level = if (_currentGameMode.value == GameMode.FUSION_TETRIS && Math.random() < 0.15) 2 else 1
        )

        // Game over conditional check
        if (!isValidPosition(piece.matrix, piece.position.row, piece.position.col)) {
            triggerGameOver()
        } else {
            _activePiece.value = piece
        }
    }

    private fun triggerGameOver() {
        if (_currentGameMode.value == GameMode.ELEMENTRIS && _shieldActive.value) {
            _shieldActive.value = false
            // protect once
            addLog("💎 Shield protection consumed! Board top 4 rows vaporized!")
            vaporizeTopRows(4)
            return
        }

        if (_currentGameMode.value == GameMode.COMBO_STACKER) {
            val shieldCharm = _relics.value.firstOrNull { it.id == "relic_shield" }
            if (shieldCharm?.purchased == true) {
                // auto consume
                addLog("🛡️ Shield Charm activated! Row 15-20 vaporized.")
                vaporizeBottomRows(6)
                _relics.value = _relics.value.map { if (it.id == "relic_shield") it.copy(purchased = false) else it }
                return
            }
        }

        // True over
        _isGameOver.value = true
        addLog("💀 GAME OVER! Score: ${_score.value}")
        saveGameStats()
    }

    private fun vaporizeTopRows(count: Int) {
        val board = _grid.value.map { it.clone() }.toTypedArray()
        for (r in 0 until count) {
            for (c in 0 until cols) {
                board[r][c] = CellState()
            }
        }
        _grid.value = board
        spawnPiece()
    }

    private fun vaporizeBottomRows(count: Int) {
        val board = _grid.value.map { it.clone() }.toTypedArray()
        for (r in (rows - count) until rows) {
            for (c in 0 until cols) {
                board[r][c] = CellState()
            }
        }
        _grid.value = board
        spawnPiece()
    }

    fun hold() {
        if (_isGameOver.value || _isWon.value || hasHeldThisTurn) return
        val current = _activePiece.value ?: return

        val previouslyHeld = _holdPiece.value
        _holdPiece.value = current.shape
        hasHeldThisTurn = true

        if (previouslyHeld == null) {
            spawnPiece()
        } else {
            val matrix = previouslyHeld.matrix
            val pos = BlockPosition(0, 5 - (matrix[0].size / 2))
            _activePiece.value = ActivePiece(
                shape = previouslyHeld,
                matrix = matrix,
                position = pos,
                element = BlockElement.values().random(),
                level = 1
            )
        }
        addLog("↩️ Piece put on Hold: ${TetrominoShape.nameOf(current.shape)}")
    }

    fun rotate() {
        if (_isGameOver.value || _isWon.value) return
        val current = _activePiece.value ?: return
        val rotatedMatrix = current.rotateClockwise()
        if (isValidPosition(rotatedMatrix, current.position.row, current.position.col)) {
            _activePiece.value = current.copy(matrix = rotatedMatrix)
        } else {
            // kick sliding left or right to avoid stuck walls
            if (isValidPosition(rotatedMatrix, current.position.row, current.position.col - 1)) {
                current.position.col -= 1
                _activePiece.value = current.copy(matrix = rotatedMatrix)
            } else if (isValidPosition(rotatedMatrix, current.position.row, current.position.col + 1)) {
                current.position.col += 1
                _activePiece.value = current.copy(matrix = rotatedMatrix)
            }
        }
    }

    fun moveLeft() {
        if (_isGameOver.value || _isWon.value) return
        val current = _activePiece.value ?: return
        if (isValidPosition(current.matrix, current.position.row, current.position.col - 1)) {
            current.position.col -= 1
            _activePiece.value = current.copy()
        }
    }

    fun moveRight() {
        if (_isGameOver.value || _isWon.value) return
        val current = _activePiece.value ?: return
        if (isValidPosition(current.matrix, current.position.row, current.position.col + 1)) {
            current.position.col += 1
            _activePiece.value = current.copy()
        }
    }

    fun softDrop() {
        if (_isGameOver.value || _isWon.value) return
        tick()
    }

    fun hardDrop() {
        if (_isGameOver.value || _isWon.value) return
        val current = _activePiece.value ?: return
        var currRow = current.position.row
        while (isValidPosition(current.matrix, currRow + 1, current.position.col)) {
            currRow++
        }
        current.position.row = currRow
        _activePiece.value = current
        placeActiveBlock()
    }

    fun tick() {
        val current = _activePiece.value ?: return
        if (isValidPosition(current.matrix, current.position.row + 1, current.position.col)) {
            current.position.row += 1
            _activePiece.value = current.copy()
        } else {
            placeActiveBlock()
        }
    }

    private fun isValidPosition(matrix: List<List<Int>>, r: Int, c: Int): Boolean {
        val h = matrix.size
        val w = matrix[0].size
        val board = _grid.value

        for (i in 0 until h) {
            for (j in 0 until w) {
                if (matrix[i][j] != 0) {
                    val targetR = r + i
                    val targetC = c + j

                    if (targetR < 0 || targetR >= rows || targetC < 0 || targetC >= cols) {
                        return false
                    }
                    if (board[targetR][targetC].filled) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun placeActiveBlock() {
        val current = _activePiece.value ?: return
        val board = _grid.value.map { it.clone() }.toTypedArray()
        val h = current.matrix.size
        val w = current.matrix[0].size

        var placedCorner = false

        // Theme colors matching skin or current element
        val blockColor = getBlockColorForCurrentSkin(current)

        for (i in 0 until h) {
            for (j in 0 until w) {
                if (current.matrix[i][j] != 0) {
                    val targetR = current.position.row + i
                    val targetC = current.position.col + j
                    if (targetR in 0 until rows && targetC in 0 until cols) {
                        val isCorner = (targetC == 0 || targetC == 9) && (targetR >= rows - 2)
                        if (isCorner) placedCorner = true

                        board[targetR][targetC] = CellState(
                            filled = true,
                            color = blockColor,
                            element = current.element,
                            level = current.level,
                            isCorner = isCorner
                        )
                    }
                }
            }
        }

        _grid.value = board
        _activePiece.value = null

        // Calculate score for placement of the specific shape
        var placementScore = current.shape.baseScore
        var multiplierAdd = 0f

        // Mode Rules Apply:
        when (_currentGameMode.value) {
            GameMode.TETRATRO -> {
                // T-Joker
                val tJoker = _jokers.value.firstOrNull { it.id == "joker_t" }
                if (tJoker?.active == true && current.shape == TetrominoShape.T) {
                    placementScore = (placementScore * 1.5).toInt()
                    multiplierAdd += 0.2f
                    addLog("🃏 T-Joker triggered! +50% T-block score.")
                }

                // Corner Booster Joker
                val cornerJoker = _jokers.value.firstOrNull { it.id == "joker_corner" }
                if (cornerJoker?.active == true && placedCorner) {
                    placementScore += 100
                    multiplierAdd += 0.1f
                    addLog("🎯 Corner Booster Joker! +100 bonus.")
                }
            }
            GameMode.FACTORY_BLOCKS -> {
                // Placements yield instant minor factory score
                placementScore = (placementScore * 0.5).toInt()
                _factoryCredits.value += 1.5f
            }
            GameMode.FUSION_TETRIS -> {
                // Fusion Tetris adjacently placed blocks combine!
                val merges = handleFusionMerges()
                if (merges > 0) {
                    placementScore += (merges * 300)
                    _coins.value += (merges * 5)
                    multiplierAdd += (merges * 0.3f)
                }
            }
            GameMode.TETRIS_CASINO -> {
                // Casino gamble variable multiplier
                if (_casinoHighRiskMode.value) {
                    val rand = (0.5 + Math.random() * 4.5).toFloat() // 0.5x to 5.0x
                    _multiplier.value = rand
                    addLog("🎲 Casino High Risk randomized multi: ${String.format("%.2f", rand)}x !")
                }
            }
            else -> {}
        }

        _score.value += (placementScore * _multiplier.value).toInt()
        if (multiplierAdd > 0) {
            _multiplier.value += multiplierAdd
        }

        // Handle line clears and elements interaction
        checkLineClears()

        // Spawns Next
        spawnPiece()
    }

    private fun getBlockColorForCurrentSkin(piece: ActivePiece): Color {
        return when (activeSkinId.value) {
            "skin_cyberpunk" -> Color(0xFF00E5FF) // neon blue
            "skin_inferno" -> Color(0xFFFF3D00) // fire orange
            "skin_forest" -> Color(0xFF00E676) // foliage green
            "skin_neon_matrix" -> Color(0xFF39FF14) // radioactive matrix green_light
            else -> piece.element.color // standard element colors
        }
    }

    // Fusion mechanism: searches the stack for adjacent cells with the same color/tier and merges them
    private fun handleFusionMerges(): Int {
        val board = _grid.value.map { it.clone() }.toTypedArray()
        var merges = 0

        // Search for horizontal blocks with matching level and element
        for (r in 0 until rows) {
            for (c in 0 until cols - 1) {
                val current = board[r][c]
                val right = board[r][c+1]
                if (current.filled && right.filled && current.level == right.level && current.element == right.element && current.level < 5) {
                    // merge right into left
                    board[r][c] = current.copy(level = current.level + 1)
                    board[r][c+1] = CellState() // empty right
                    merges++
                    prefs.totalFusionsMerged += 1
                    addLog("🧪 FUSION! Dual Lv.${current.level} merged into Lv.${current.level + 1} ⭐!")
                }
            }
        }
        _grid.value = board
        return merges
    }

    private fun checkLineClears() {
        var board = _grid.value.map { it.clone() }.toTypedArray()
        var clearedLinesCount = 0

        val linesToClear = mutableListOf<Int>()
        for (r in 0 until rows) {
            var full = true
            for (c in 0 until cols) {
                if (!board[r][c].filled) {
                    full = false
                    break
                }
            }
            if (full) {
                linesToClear.add(r)
            }
        }

        if (linesToClear.isNotEmpty()) {
            clearedLinesCount = linesToClear.size
            comboChain++

            // Analyze elements in the cleared lines (important for Elementris)
            val elementsFound = mutableSetOf<BlockElement>()
            for (r in linesToClear) {
                for (c in 0 until cols) {
                    board[r][c].element?.let { elementsFound.add(it) }
                }
            }

            // High resolution scores
            var baseClearScore = when (clearedLinesCount) {
                1 -> 150
                2 -> 400
                3 -> 900
                4 -> 2000
                else -> 2500
            }

            var lineMultiplier = 1.0f

            // Game Mode rule modifiers for line clears:
            when (_currentGameMode.value) {
                GameMode.TETRATRO -> {
                    // Foil Joker flat bonus
                    val foilJoker = _jokers.value.firstOrNull { it.id == "joker_foil" }
                    if (foilJoker?.active == true) {
                        baseClearScore += 200
                        addLog("✨ Foil Joker added +200 flat points!")
                    }

                    // Quad Multiplier 4 line clear bonus
                    if (clearedLinesCount == 4) {
                        val quadJoker = _jokers.value.firstOrNull { it.id == "joker_quad" }
                        if (quadJoker?.active == true) {
                            lineMultiplier *= 3.0f
                            addLog("⚡ Quad Streaker triggered! Clearing 4 rows gives 3x multiplier!")
                        }
                    }
                }
                GameMode.ELEMENTRIS -> {
                    // Combination effects: Fire + Lightning = Explosion!
                    if (elementsFound.contains(BlockElement.FIRE) && elementsFound.contains(BlockElement.LIGHTNING)) {
                        baseClearScore += 400
                        lineMultiplier *= 1.5f
                        addLog("💥 ELEMENTAL COMBO: Fire + Lightning Explosion! +1.5x multi")
                        // Clear random upper cluster line if possible
                        val bonusLine = (linesToClear.minOrNull() ?: 5) - 1
                        if (bonusLine in 0 until rows) {
                            linesToClear.add(bonusLine)
                            clearedLinesCount++
                        }
                    }

                    // Combination effects: Water + Earth = Shield wall protects!
                    if (elementsFound.contains(BlockElement.WATER) && elementsFound.contains(BlockElement.EARTH)) {
                        _shieldActive.value = true
                        addLog("🛡️ CRYSTALLINE WALL: Earth + Water Shield Activated!")
                    }
                }
                GameMode.COMBO_STACKER -> {
                    // Consecutive combo multipliers
                    lineMultiplier += (comboChain * 0.5f)
                    addLog("📈 COMBO SPREE x$comboChain! clear multiplier: ${lineMultiplier}x")
                    _coins.value += (clearedLinesCount * 12)
                }
                GameMode.DUNGEON_TETRIS -> {
                    // Rogue Dungeon RPG skill interactions
                    var damageInvested = 0
                    if (clearedLinesCount >= 4) {
                        // Pierce Attack
                        damageInvested = 150
                        addLog("⚔️ I-Shape PIERCE SLASH! Imposed $damageInvested damage to ${_monsterName.value}!")
                    } else if (clearedLinesCount == 3) {
                        // Area blast
                        damageInvested = 90
                        addLog("🔥 T-Shape METEOR BURST! Imposed $damageInvested damage!")
                    } else {
                        // Basic attack
                        damageInvested = clearedLinesCount * 30
                        addLog("🗡️ Slash attack! Imposed $damageInvested damage!")
                    }

                    // Healing
                    if (elementsFound.contains(BlockElement.WATER)) {
                        _playerHp.value = (_playerHp.value + 20).coerceAtMost(100)
                        addLog("💚 Holy water healed +20 HP!")
                    }

                    // Attack Monster
                    _monsterHp.value = (_monsterHp.value - damageInvested).coerceAtLeast(0)
                    if (_monsterHp.value <= 0) {
                        defeatDungeonMonster()
                    }
                }
                GameMode.TETRIS_CASINO -> {
                    // Double score gamble probability
                    if (Math.random() < 0.25) {
                        addLog("🎰 Casino jackpot alert! Doubling this clear!")
                        lineMultiplier *= 2.0f
                        prefs.totalGamblingWins += 1
                    }
                }
                GameMode.FACTORY_BLOCKS -> {
                    // yield production units
                    _factoryCredits.value += (clearedLinesCount * 30f)
                }
                else -> {}
            }

            // Apply clear score
            val addition = (baseClearScore * _multiplier.value * lineMultiplier).toInt()
            _score.value += addition
            _coins.value += clearedLinesCount * 10
            prefs.totalLinesCleared += clearedLinesCount

            // Animate row shifting down
            val newBoard = Array(rows) { Array(cols) { CellState() } }
            var writeRow = rows - 1

            // sort linesToClear and copy valid rows
            val clearSet = linesToClear.toSet()
            for (readRow in rows - 1 downTo 0) {
                if (!clearSet.contains(readRow)) {
                    newBoard[writeRow] = board[readRow]
                    writeRow--
                }
            }
            // empty upper lines
            while (writeRow >= 0) {
                newBoard[writeRow] = Array(cols) { CellState() }
                writeRow--
            }

            _grid.value = newBoard

            // Check standard target wins
            if (_score.value >= _targetScore.value) {
                triggerVictory()
            }
        } else {
            // No lines cleared breaks combo
            comboChain = 0
        }
    }

    private fun defeatDungeonMonster() {
        prefs.totalMonstersDefeated += 1
        val monsterCount = prefs.totalMonstersDefeated
        _score.value += 1000
        _coins.value += 50
        addLog("🏆 SLAYED ${_monsterName.value}! +1000 points!")

        // Progress to next monster
        val nextMonster = when (monsterCount % 4) {
            1 -> Pair("Skeleton Archer", 450)
            2 -> Pair("Fire Wyvern", 800)
            3 -> Pair("Giga Dragon", 2000)
            else -> Pair("Green Slime Prime", 350)
        }
        _monsterName.value = nextMonster.first
        _monsterMaxHp.value = nextMonster.second
        _monsterHp.value = nextMonster.second
        addLog("👹 Spawned next Challenger: ${nextMonster.first} (${nextMonster.second} HP)!")
    }

    private fun triggerVictory() {
        _isWon.value = true
        addLog("🏆 WINNER! Score: ${_score.value} reached threshold ${_targetScore.value}!")
        saveGameStats()
    }

    // Casino Risk Button click
    fun gambleCasino() {
        if (_currentGameMode.value != GameMode.TETRIS_CASINO || _isGameOver.value || _isWon.value) return

        if (Math.random() < 0.5) {
            val doubled = _score.value * 2
            _score.value = doubled
            prefs.totalGamblingWins += 1
            addLog("🎲 DOUBLE OR NOTHING SUCCESS! Score doubled to $doubled!")
        } else {
            val halved = _score.value / 2
            _score.value = halved
            addLog("🟥 GONE BUST! Casino cut your score to $halved!")
        }

        if (_score.value >= _targetScore.value) {
            triggerVictory()
        }
    }

    fun toggleCasinoHighRisk() {
        _casinoHighRiskMode.value = !_casinoHighRiskMode.value
        addLog("🎲 Casino High Risk: ${_casinoHighRiskMode.value}")
    }

    // Spend credits in Factory Automation mode to purchase machines
    fun buyFactoryMiner() {
        if (_factoryCredits.value >= 30f) {
            _factoryCredits.value -= 30f
            _minerCount.value += 1
            addLog("🏭 Bought score Miner (+5 score/sec, +2.5 passive creds/sec)")
        }
    }

    fun buyFactorySweeper() {
        if (_factoryCredits.value >= 60f) {
            _factoryCredits.value -= 60f
            _sweeperCount.value += 1
            addLog("🧹 Bought auto-Sweeper turbine (cleans random bottom row block over clock)")
        }
    }

    // Purchase items in Shop pauses (Combo Stacker Relics / Tetratro Jokers)
    fun purchaseJoker(jokerId: String) {
        val list = _jokers.value.toMutableList()
        val index = list.indexOfFirst { it.id == jokerId }
        if (index != -1) {
            val joker = list[index]
            if (!joker.active && _coins.value >= joker.cost) {
                _coins.value -= joker.cost
                list[index] = joker.copy(active = true)
                _jokers.value = list
                addLog("🃏 Activated Joker: ${joker.name}")
            }
        }
    }

    fun purchaseRelicInGame(relicId: String) {
        val list = _relics.value.toMutableList()
        val index = list.indexOfFirst { it.id == relicId }
        if (index != -1) {
            val relic = list[index]
            if (!relic.purchased && _coins.value >= relic.cost) {
                _coins.value -= relic.cost
                list[index] = relic.copy(purchased = true)
                _relics.value = list
                addLog("🔑 Purchased Relic: ${relic.name}")
            }
        }
    }

    private fun saveGameStats() {
        prefs.saveHighScore(_currentGameMode.value.id, _score.value)
        prefs.globalCoins += (_score.value / 15) + _coins.value

        // Check Achievements Update
        checkAchievementsProg()
    }

    private fun checkAchievementsProg() {
        val prevPlayed = prefs.totalGamesPlayed
        val prevLines = prefs.totalLinesCleared
        val prevMonsters = prefs.totalMonstersDefeated
        val prevGambles = prefs.totalGamblingWins

        // Update Achievements matching progress
        val achList = listOf(
            Triple("ach_rookie", prevPlayed, 1),
            Triple("ach_pro", prevPlayed, 10),
            Triple("ach_sweeper", prevLines, 40),
            Triple("ach_slayer", prevMonsters, 3),
            Triple("ach_casino", prevGambles, 3)
        )

        for (ach in achList) {
            prefs.saveAchievementProgress(ach.first, ach.second)
            if (ach.second >= ach.third && !prefs.isAchievementUnlocked(ach.first)) {
                prefs.setAchievementUnlocked(ach.first, true)
                addLog("🎯 ACHIEVEMENT UNLOCKED: ${ach.first.replace("ach_", "").uppercase()}!")
            }
        }
    }

    override fun onCleared() {
        gameJob?.cancel()
        super.onCleared()
    }
}
