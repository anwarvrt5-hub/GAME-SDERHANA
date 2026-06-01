package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.*
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.delay

enum class ActiveScreen {
    MAIN,
    PLAY_CHOOSE,
    GAME_PLAY,
    COLLECTION,
    STATISTICS,
    ACHIEVEMENTS,
    UNLOCKS,
    COSMETICS,
    LEADERBOARDS,
    SETTINGS,
    HOW_TO_PLAY
}

@Composable
fun TetratroGameApp(
    viewModel: GameViewModel,
    onExitPressed: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(ActiveScreen.MAIN) }
    var selectedGameMode by remember { mutableStateOf(GameMode.TETRATRO) }
    val context = LocalContext.current
    val prefs = remember { GamePreferences(context) }

    // Navigation back stack logic
    val navigateTo: (ActiveScreen) -> Unit = { target ->
        currentScreen = target
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B19),
                        Color(0xFF0F172A),
                        Color(0xFF1E1E38)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Beautiful retro neon star particles animating in background
        RetroGridAnimationBackground()

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                    animationSpec = tween(220),
                    initialOffsetX = { 300 }
                )).togetherWith(
                    fadeOut(animationSpec = tween(180)) + slideOutHorizontally(
                        animationSpec = tween(180),
                        targetOffsetX = { -300 }
                    )
                )
            },
            label = "screen_navigation"
        ) { screen ->
            when (screen) {
                ActiveScreen.MAIN -> MainMenuScreen(
                    prefs = prefs,
                    onNavigate = navigateTo,
                    onPlaySelect = {
                        navigateTo(ActiveScreen.PLAY_CHOOSE)
                    },
                    onExit = onExitPressed
                )
                ActiveScreen.PLAY_CHOOSE -> ModeSelectionScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) },
                    onSelectMode = { mode ->
                        selectedGameMode = mode
                        viewModel.startNewGame(mode)
                        navigateTo(ActiveScreen.GAME_PLAY)
                    }
                )
                ActiveScreen.GAME_PLAY -> GamePlayScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    mode = selectedGameMode,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.COLLECTION -> CollectionScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.STATISTICS -> StatisticsScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.ACHIEVEMENTS -> AchievementsScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.UNLOCKS -> UnlocksScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.COSMETICS -> CosmeticsScreen(
                    viewModel = viewModel,
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.LEADERBOARDS -> LeaderboardsScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.SETTINGS -> SettingsScreen(
                    prefs = prefs,
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
                ActiveScreen.HOW_TO_PLAY -> HowToPlayScreen(
                    onBack = { navigateTo(ActiveScreen.MAIN) }
                )
            }
        }
    }
}

@Composable
fun RetroGridAnimationBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background_grid")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_moving"
    )

    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val purpleNeon = Color(0x1F8B5CF6)
                val strokeWidth = 1.dp.toPx()

                // Draw vertical grid lines
                var col = 0f
                while (col < size.width) {
                    drawLine(
                        color = purpleNeon,
                        start = Offset(col, 0f),
                        end = Offset(col, size.height),
                        strokeWidth = strokeWidth
                    )
                    col += 60f
                }

                // Draw horizontal grid lines shifting down
                var row = gridOffset
                while (row < size.height) {
                    drawLine(
                        color = purpleNeon,
                        start = Offset(0f, row),
                        end = Offset(size.width, row),
                        strokeWidth = strokeWidth
                    )
                    row += 60f
                }
            }
    )
}

// ========================
//      MAIN MENU
// ========================
@Composable
fun MainMenuScreen(
    prefs: GamePreferences,
    onNavigate: (ActiveScreen) -> Unit,
    onPlaySelect: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // Glowing Cyberpunk Title Border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF13132B).copy(alpha = 0.8f))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFF8B5CF6))), RoundedCornerShape(16.dp))
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "=======================",
                    color = Color(0xFFA78BFA),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "TETRATRO",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFFF43F5E),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "=======================",
                    color = Color(0xFFA78BFA),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Multiplier Coins Count Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B35).copy(alpha = 0.9f)),
            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Token coins",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Global Balance",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "${prefs.globalCoins} 🪙",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Standard Menu Entries List (From direct user mandate)
        Column(
            modifier = Modifier.width(280.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MenuButton("▶ Play", Color(0xFF10B981), onClick = onPlaySelect)
            MenuButton("🏆 Collection", Color(0xFF3B82F6), onClick = { onNavigate(ActiveScreen.COLLECTION) })
            MenuButton("📈 Statistics", Color(0xFF8B5CF6), onClick = { onNavigate(ActiveScreen.STATISTICS) })
            MenuButton("🎯 Achievements", Color(0xFFF59E0B), onClick = { onNavigate(ActiveScreen.ACHIEVEMENTS) })
            MenuButton("🛒 Unlocks", Color(0xFFEC4899), onClick = { onNavigate(ActiveScreen.UNLOCKS) })
            MenuButton("🎨 Cosmetics", Color(0xFF14B8A6), onClick = { onNavigate(ActiveScreen.COSMETICS) })
            MenuButton("🌐 Leaderboards", Color(0xFF6366F1), onClick = { onNavigate(ActiveScreen.LEADERBOARDS) })
            MenuButton("⚙ Settings", Color(0xFF64748B), onClick = { onNavigate(ActiveScreen.SETTINGS) })
            MenuButton("📖 How To Play", Color(0xFFA855F7), onClick = { onNavigate(ActiveScreen.HOW_TO_PLAY) })
            MenuButton("🚪 Exit", Color(0xFFEF4444), onClick = onExit)
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun MenuButton(
    text: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.9f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("menu_btn_${text.replace(" ", "_").lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Left
                ),
                color = Color.White,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

// ========================
//      SELECT GAME MODE
// ========================
@Composable
fun ModeSelectionScreen(
    prefs: GamePreferences,
    onBack: () -> Unit,
    onSelectMode: (GameMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select Tetratro Mode",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(GameMode.values()) { mode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMode(mode) }
                        .testTag("mode_card_${mode.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D35)),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "High: ${prefs.getHighScore(mode.id)}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mode.desc,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ========================
//     INTERACTIVE GAMEPLAY SCREEN
// ========================
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    prefs: GamePreferences,
    mode: GameMode,
    onBack: () -> Unit
) {
    val grid by viewModel.grid.collectAsState()
    val activePiece by viewModel.activePiece.collectAsState()
    val nextPiece by viewModel.nextPiece.collectAsState()
    val holdPiece by viewModel.holdPiece.collectAsState()
    val score by viewModel.score.collectAsState()
    val multiplier by viewModel.multiplier.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val targetScore by viewModel.targetScore.collectAsState()
    val gameOver by viewModel.isGameOver.collectAsState()
    val won by viewModel.isWon.collectAsState()
    val logs by viewModel.eventLogs.collectAsState()

    // Mode specific variables
    // Mode 1: Tetratro Jokers
    val jokers by viewModel.jokers.collectAsState()
    // Mode 2: Elementris Shield
    val shieldActive by viewModel.shieldActive.collectAsState()
    // Mode 3: Combo Stacker Relics
    val relics by viewModel.relics.collectAsState()
    // Mode 4: Dungeon Monster RPG
    val monsterHp by viewModel.monsterHp.collectAsState()
    val monsterMaxHp by viewModel.monsterMaxHp.collectAsState()
    val monsterName by viewModel.monsterName.collectAsState()
    val playerHp by viewModel.playerHp.collectAsState()
    // Mode 5: Casino High Risk
    val casinoRisk by viewModel.casinoHighRiskMode.collectAsState()
    // Mode 6: Factory Automator
    val credits by viewModel.factoryCredits.collectAsState()
    val miners by viewModel.minerCount.collectAsState()
    val sweepers by viewModel.sweeperCount.collectAsState()

    LaunchedEffect(gameOver, won) {
        if (gameOver || won) {
            // autosave high score and stats automatically in viewModel
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .testTag("game_screen")
    ) {
        // App top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
            }
            Text(
                text = "${mode.title} Play",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Button(
                onClick = { viewModel.startNewGame(mode) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text("Restart", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar for score threshold
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Score: $score",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Target: $targetScore",
                    color = Color(0xFFF43F5E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { (score.toFloat() / targetScore).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF10B981),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Board and Queue Column
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // LEFT COLUMN: Game Grid Board
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .background(Color(0xFF0C1021))
                    .border(2.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Game Cell Draw Grid (10 columns, 20 rows)
                Column(modifier = Modifier.fillMaxSize()) {
                    for (r in 0 until 20) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            for (c in 0 until 10) {
                                // Draw active piece cells or steady cells
                                val isCellActive = isActiveCell(activePiece, r, c)
                                val cellColor = getCellColor(grid, activePiece, r, c)
                                val cellLevel = getCellLevel(grid, activePiece, r, c)
                                val cellElement = getCellElement(grid, activePiece, r, c)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(1.dp)
                                        .background(
                                            color = cellColor,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .border(
                                            width = 0.5.dp,
                                            color = if (cellColor != Color.Transparent) Color.Black.copy(alpha = 0.3f) else Color(0xFF1E293B)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cellColor != Color.Transparent) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            if (cellLevel > 1) {
                                                Text(
                                                    text = "⭐$cellLevel",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            } else if (cellElement != null && mode == GameMode.ELEMENTRIS) {
                                                Text(
                                                    text = cellElement.icon,
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Victory/GameOver Layer
                if (gameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💀 BUSTED!", color = Color.Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Score: $score", color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.startNewGame(mode) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                if (won) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 CONQUERED!", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Text("Your final score beat the round target!", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.startNewGame(mode) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Play Next")
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: Queues, Mode Specific Details, Log
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // HOLD & NEXT CARD
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Hold Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("HOLD", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                holdPiece?.let { ShapePreviewMini(it) } ?: Text("--", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                            }
                        }
                    }

                    // Next Box
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("NEXT", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                ShapePreviewMini(nextPiece)
                            }
                        }
                    }
                }

                // STATS INFO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31))
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Multi:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("${String.format("%.1f", multiplier)}x", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coins:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("$coins 🪙", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                        if (mode == GameMode.ELEMENTRIS && shieldActive) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Shield:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("ACTIVE 💎", fontSize = 11.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // MODE SPECIFIC HUD / TRIGGER BUTTONS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    when (mode) {
                        GameMode.TETRATRO -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("ACTIVE JOKERS", fontSize = 10.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold)
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(jokers) { joker ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (joker.active) Color(0xFF1E1E38) else Color(0x33334155))
                                                .clickable { viewModel.purchaseJoker(joker.id) }
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(joker.icon, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(joker.name, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                Text(joker.desc, fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
                                            }
                                            if (!joker.active) {
                                                Text("${joker.cost}🪙", fontSize = 8.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                            } else {
                                                Text("ON", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        GameMode.ELEMENTRIS -> {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔮 ELEMENTRIS", fontSize = 11.sp, color = Color(0xFFFFD740), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stack elements together! Clearing Fire+Lightning triggers explosions. Water+Earth gives shielding walls.",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        GameMode.COMBO_STACKER -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text("SHOP RELICS", fontSize = 10.sp, color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(relics) { relic ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (relic.purchased) Color(0xFF1E1E38) else Color(0x33334155))
                                                .clickable { viewModel.purchaseRelicInGame(relic.id) }
                                                .padding(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(relic.icon, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(relic.name, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            if (!relic.purchased) {
                                                Text("${relic.cost}🪙", fontSize = 8.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                            } else {
                                                Text("GOT", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        GameMode.DUNGEON_TETRIS -> {
                            // RPG HP bars
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("⚔️ DUNGEON COMBAT", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)

                                // Monster HP Area
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444))) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(monsterName, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("$monsterHp / $monsterMaxHp HP", fontSize = 8.sp, color = Color.White)
                                        }
                                        LinearProgressIndicator(
                                            progress = { (monsterHp.toFloat() / monsterMaxHp).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = Color.Red,
                                            trackColor = Color.Red.copy(alpha = 0.2f)
                                        )
                                    }
                                }

                                // Player HP Area
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0x3310B981))) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Player (Hero)", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("$playerHp / 100 HP", fontSize = 8.sp, color = Color.White)
                                        }
                                        LinearProgressIndicator(
                                            progress = { (playerHp.toFloat() / 100).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = Color.Green,
                                            trackColor = Color.Green.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                        GameMode.TETRIS_CASINO -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("🎲 CASINO SECTOR", fontSize = 10.sp, color = Color(0xFFFFD740), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))

                                // Double-or-nothing action button
                                Button(
                                    onClick = { viewModel.gambleCasino() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.fillMaxWidth().height(30.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Double or Nothing!", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // High Risk Mode Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("High Risk Multipliers", fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f))
                                    Switch(
                                        checked = casinoRisk,
                                        onCheckedChange = { viewModel.toggleCasinoHighRisk() },
                                        modifier = Modifier.scale(0.6f)
                                    )
                                }
                            }
                        }
                        GameMode.FACTORY_BLOCKS -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("FACTORY UNITS", fontSize = 10.sp, color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                                    Text("${String.format("%.1f", credits)} cr", fontSize = 10.sp, color = Color(0xFF06B6D4), fontFamily = FontFamily.Monospace)
                                }

                                // Buy Miner item
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33334155))
                                        .clickable { viewModel.buyFactoryMiner() }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("👨‍💻 Miner (Qty: $miners)", fontSize = 8.sp, color = Color.White)
                                    Text("Cost: 30 cr", fontSize = 8.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }

                                // Buy Sweeper item
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x33334155))
                                        .clickable { viewModel.buyFactorySweeper() }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🧹 Sweeper (Qty: $sweepers)", fontSize = 8.sp, color = Color.White)
                                    Text("Cost: 60 cr", fontSize = 8.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        GameMode.FUSION_TETRIS -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🧪 FUSION MATRIX", fontSize = 11.sp, color = Color(0xFF14B8A6), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Consecutive adjacent landed cells of the matching element fuse together, increasing block level (⭐) and multiplying scores exponentiatedly!",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // EVENT TRACKING LOG
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFF090D1A), RoundedCornerShape(4.dp))
                        .padding(4.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(logs) { log ->
                            Text(
                                text = "> $log",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (log.contains("CONQUERED") || log.contains("SUCCESS")) Color(0xFF10B981) else if (log.contains("BUSTED") || log.contains("GameOver")) Color(0xFFEF4444) else Color.White.copy(alpha = 0.8f),
                                fontSize = 8.5.sp,
                                modifier = Modifier.padding(bottom = 2.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BOTTOM CONTROLS BOARD: Big Touch targets (minimum 48dp target)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // MOVEMENT CONTROLS LEFT CLUSTER
            Row(
                modifier = Modifier.weight(1.3f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // MOVE LEFT
                RetroActionButton(
                    icon = Icons.Default.ArrowBack,
                    contentDesc = "Move Left",
                    testTag = "btn_left",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.moveLeft() }
                )

                // ROTATE
                RetroActionButton(
                    icon = Icons.Default.RotateRight,
                    contentDesc = "Rotate Shape",
                    testTag = "btn_rotate",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.rotate() }
                )

                // MOVE RIGHT
                RetroActionButton(
                    icon = Icons.Default.ArrowForward,
                    contentDesc = "Move Right",
                    testTag = "btn_right",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.moveRight() }
                )
            }

            // DROP CONTROLS RIGHT CLUSTER
            Row(
                modifier = Modifier.weight(0.9f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // HOLD
                RetroActionButton(
                    icon = Icons.Default.SwapCalls,
                    contentDesc = "Hold queued Piece",
                    testTag = "btn_hold",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.hold() }
                )

                // SOFT DROP
                RetroActionButton(
                    icon = Icons.Default.ArrowDownward,
                    contentDesc = "Soft Drop Shape",
                    testTag = "btn_soft_drop",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.softDrop() }
                )

                // HARD DROP
                RetroActionButton(
                    icon = Icons.Default.KeyboardDoubleArrowDown,
                    contentDesc = "Hard Drop Shape",
                    testTag = "btn_hard_drop",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.hardDrop() }
                )
            }
        }
    }
}

@Composable
fun ShapePreviewMini(shape: TetrominoShape) {
    val matrix = shape.matrix
    val rows = matrix.size
    val cols = matrix[0].size
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for (i in 0 until rows) {
            Row {
                for (j in 0 until cols) {
                    val active = matrix[i][j] != 0
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .padding(0.5.dp)
                            .background(
                                if (active) Color(0xFFF43F5E) else Color.Transparent,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun RetroActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, Color(0xFF475569)),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(52.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            tint = Color(0xFFA78BFA),
            modifier = Modifier.size(24.dp)
        )
    }
}

// Helpers for active falling piece grid representation
fun isActiveCell(active: ActivePiece?, r: Int, c: Int): Boolean {
    if (active == null) return false
    val h = active.matrix.size
    val w = active.matrix[0].size
    val pieceR = r - active.position.row
    val pieceC = c - active.position.col
    if (pieceR in 0 until h && pieceC in 0 until w) {
        return active.matrix[pieceR][pieceC] != 0
    }
    return false
}

fun getCellColor(grid: Array<Array<CellState>>, active: ActivePiece?, r: Int, c: Int): Color {
    if (isActiveCell(active, r, c)) {
        return active?.element?.color ?: Color(0xFF6366F1)
    }
    return grid[r][c].color
}

fun getCellLevel(grid: Array<Array<CellState>>, active: ActivePiece?, r: Int, c: Int): Int {
    if (isActiveCell(active, r, c)) {
        return active?.level ?: 1
    }
    return grid[r][c].level
}

fun getCellElement(grid: Array<Array<CellState>>, active: ActivePiece?, r: Int, c: Int): BlockElement? {
    if (isActiveCell(active, r, c)) {
        return active?.element
    }
    return grid[r][c].element
}


// ========================
//      CARDS COLLECTION SCREEN
// ========================
@Composable
fun CollectionScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    val itemsDeck = listOf(
        JokerCard("joker_t", "T-Joker", "T-shapes get +50% multiplier", 80, active = true, icon = "🃏"),
        JokerCard("joker_quad", "Quad Streaker", "Clearing 4 lines gives x3 multiplier", 110, active = false, icon = "⚡"),
        JokerCard("joker_corner", "Corner Booster", "Landed blocks touching corners get +100 bonus", 90, active = false, icon = "🎯"),
        JokerCard("joker_foil", "Foil Card", "+200 flat points on any line clears", 130, active = false, icon = "✨")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🃏 Joker Cards Collection",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "A complete index of tactical Jokers custom built to alter basic Tetratro score rules similar to Balatro structure:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            items(itemsDeck) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151C30)),
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E1065)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.icon, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.desc, color = Color.White.copy(alpha = 0.65f), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}


// ========================
//     STATISTICS SCREEN
// ========================
@Composable
fun StatisticsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📈 Tetratro Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticRow("Total Games Played", "${prefs.totalGamesPlayed}")
                StatisticRow("Total lines Cleared", "${prefs.totalLinesCleared}")
                StatisticRow("Monsters Slain (Dungeon mode)", "${prefs.totalMonstersDefeated}")
                StatisticRow("Gambles Succeeded (Casino mode)", "${prefs.totalGamblingWins}")
                StatisticRow("Fusion Blocks Synced", "${prefs.totalFusionsMerged}")
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                StatisticRow("Saved High Score (Balatro)", "${prefs.getHighScore("tetratro")}")
                StatisticRow("Saved High Score (Casino)", "${prefs.getHighScore("tetris_casino")}")
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(
            value,
            color = Color(0xFFA78BFA),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ========================
//     ACHIEVEMENTS SCREEN
// ========================
@Composable
fun AchievementsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    val achList = listOf(
        Achievement("ach_rookie", "Arcade Debut", "Complete your initial run in any mode", prefs.getAchievementProgress("ach_rookie"), 1),
        Achievement("ach_pro", "Venerated Tactician", "Play 10 rounds of Tetratro", prefs.getAchievementProgress("ach_pro"), 10),
        Achievement("ach_sweeper", "Labyrinth Sweeper", "Clear 40 grid rows in standard matches", prefs.getAchievementProgress("ach_sweeper"), 40),
        Achievement("ach_slayer", "Rogue Slayer", "Slay 3 Dungeon RPG monsters", prefs.getAchievementProgress("ach_slayer"), 3),
        Achievement("ach_casino", "Jackpot Gambler", "Win 3 Double-or-Nothing Casino risks", prefs.getAchievementProgress("ach_casino"), 3)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🎯 Achievements",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achList) { ach ->
                val alreadyUnlocked = prefs.isAchievementUnlocked(ach.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alreadyUnlocked) Color(0xFF132F23) else Color(0xFF131D31)
                    ),
                    border = BorderStroke(1.dp, if (alreadyUnlocked) Color(0xFF10B981) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ach.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ach.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ach.desc,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { (ach.progress.toFloat() / ach.target).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = if (alreadyUnlocked) Color(0xFF10B981) else Color(0xFF6366F1),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (alreadyUnlocked) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = Color(0xFF10B981))
                        } else {
                            Text(
                                text = "${ach.progress}/${ach.target}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}


// ========================
//     STORE UNLOCKS SCREEN
// ========================
@Composable
fun UnlocksScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    var globalBalance by remember { mutableStateOf(prefs.globalCoins) }

    val shopList = listOf(
        JokerCard("joker_quad", "Quad Streaker Card", "Clears 4 Lines Multi ×3", 150, icon = "⚡"),
        JokerCard("joker_corner", "Corner Booster Card", "Corner Lands give +100 bonus", 180, icon = "🎯"),
        JokerCard("joker_foil", "Foil Card", "Gives +200 flat points on line clears", 220, icon = "✨")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🛒 Store Unlocks",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0x33FFD700))) {
                Text(
                    text = "$globalBalance 🪙",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Unlock locked premium jokers using gold earned during round plays to permanently make them purchasable during gameplay in standard matches!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            items(shopList) { item ->
                val isUnlockedValue = prefs.isItemUnlocked(item.id)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    border = BorderStroke(1.dp, if (isUnlockedValue) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF475569))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(item.desc, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isUnlockedValue) {
                            Text("UNLOCKED", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Button(
                                onClick = {
                                    if (globalBalance >= item.cost) {
                                        prefs.globalCoins -= item.cost
                                        prefs.setItemUnlocked(item.id, true)
                                        globalBalance = prefs.globalCoins
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("${item.cost}🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ========================
//     COSMETICS SKIN SCREEN
// ========================
@Composable
fun CosmeticsScreen(
    viewModel: GameViewModel,
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    var coinsBy by remember { mutableStateOf(prefs.globalCoins) }
    var currentEquipped by remember { mutableStateOf(prefs.equippedSkinId) }

    val skinDeck = listOf(
        CosmeticSkin("skin_classic", "Standard Element", "Visual based on elemental block blocks", 0, Color(0xFFFF5252), Color(0xFF40C4FF), isUnlocked = true),
        CosmeticSkin("skin_cyberpunk", "Cyberpunk Neon", "High vibrant blue grid accents", 80, Color(0xFF00E5FF), Color(0xFF0D47A1), isUnlocked = prefs.isItemUnlocked("skin_cyberpunk")),
        CosmeticSkin("skin_inferno", "Inferno Crimson", "Intense flame engine shades", 120, Color(0xFFFF3D00), Color(0xFF9E0D00), isUnlocked = prefs.isItemUnlocked("skin_inferno")),
        CosmeticSkin("skin_neon_matrix", "Neon Grid Matrix", "Braggart green radioactive bricks", 150, Color(0xFF39FF14), Color(0xFF1B5E20), isUnlocked = prefs.isItemUnlocked("skin_neon_matrix"))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🎨 Block Customizer",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0x33FFD700))) {
                Text(
                    text = "$coinsBy 🪙",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Spend coins earned on games to purchase stunning cyberpunk block glow themes. Equip any skin to instantly change visual rendering color matrices during live games:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            items(skinDeck) { skin ->
                val equipped = currentEquipped == skin.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (skin.isUnlocked) {
                                prefs.equippedSkinId = skin.id
                                viewModel.activeSkinId.value = skin.id
                                currentEquipped = skin.id
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (equipped) Color(0xFF1E1C3A) else Color(0xFF131D31)
                    ),
                    border = BorderStroke(2.dp, if (equipped) Color(0xFFA78BFA) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Preview Indicators
                        Row(modifier = Modifier.size(40.dp)) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(skin.color1, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(skin.color2, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(skin.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(skin.desc, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!skin.isUnlocked) {
                            Button(
                                onClick = {
                                    if (coinsBy >= skin.cost) {
                                        prefs.globalCoins -= skin.cost
                                        prefs.setItemUnlocked(skin.id, true)
                                        coinsBy = prefs.globalCoins
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("${skin.cost}🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (equipped) {
                                Text("ACTIVE", color = Color(0xFFA78BFA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("EQUIP", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ========================
//     LOCAL LEADERBOARDS
// ========================
@Composable
fun LeaderboardsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    val simScores = listOf(
        Triple("Grandmaster_Bal", 12500, "Tetratro"),
        Triple("Neo_Stacker", 9550, "Combo Stacker"),
        Triple("DungeonSlayer", 8200, "Dungeon Tetris"),
        Triple("RogueCasino", 6500, "Tetris Casino"),
        Triple("You (Best)", prefs.getHighScore("tetratro").coerceAtLeast(prefs.getHighScore("tetris_casino")), "Varies")
    ).sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🌐 Leaderboards",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Displaying top overall scores tracked locally. Finish modes with high scoring triggers to rise up the board positions!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            items(simScores) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    border = BorderStroke(1.dp, if (record.first.contains("You")) Color(0xFFA78BFA) else Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = record.first,
                                color = if (record.first.contains("You")) Color(0xFFA78BFA) else Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Mode: ${record.third}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        Text(
                            text = "${record.second} pts",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}


// ========================
//     SETTINGS SCREEN
// ========================
@Composable
fun SettingsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    var resetPromptVisible by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "⚙ Settings",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Game Sound Effects", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Button(
                    onClick = { resetPromptVisible = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset All High Scores & Progress", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (resetPromptVisible) {
            AlertDialog(
                onDismissRequest = { resetPromptVisible = false },
                title = { Text("Reset Progress?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you absolutely sure you want to revert all unlocks, coins, achievements, and high scores back to defaults? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            prefs.resetAll()
                            resetPromptVisible = false
                        }
                    ) {
                        Text("Reset ALL", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { resetPromptVisible = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


// ========================
//     HOW TO PLAY MANUAL SCREEN
// ========================
@Composable
fun HowToPlayScreen(
    onBack: () -> Unit
) {
    val items = listOf(
        Pair("Overview", "TETRATRO combines classical Tetris block stacking gameplay with deep tactical Roguelike system rules inspired strongly by Balatro. Play 7 exciting modes where scores rise overpoweredly!"),
        Pair("1. Tetratro Style", "Match scoring is heavily influenced by active ‘Joker Cards’. Equip multipliers, foil points and corner landed bonuses. Reach target thresholds to conquer rounds."),
        Pair("2. Elementris Mode", "Each falling piece possesses unique core elements which can combine. Clear rows with Fire+Lightning elements to explode the board! Earth+Water gives energy shielding protection from top overflow."),
        Pair("3. Combo Stacker", "Consecutive matches multiply incoming scores. Earn coins on each clear and spend them in the live Shop view during rounds to buy permanent flight speed feather relics and S/Z block reduction cards."),
        Pair("4. Dungeon Tetris", "Rogue battle mechanics. Slay incoming waves of monsters! Moving shapes deal basic damage while completing structural block layers triggers specialized attacks: I-blocks do massive pierce harm, T-blocks area damage, and O-blocks trigger heals."),
        Pair("5. Tetris Casino", "Designed for extreme risk lovers. Toggle High Risk Multiplier to completely randomize standard scoring (ranging from 0.5x up to 5x!). Hit the 'Double or Nothing' button at any time to double your entire level score on a 50/50 flip!"),
        Pair("6. Factory Blocks", "Automation-friendly mode. Position blocks to trigger resource credits generation. Settle passive credits to assemble robot score miners and automated bottom row sweepers."),
        Pair("7. Fusion Tetris", "Shed classical lines. Identical-tier stacked blocks adjacent to each other merge, leveling up into high-scoring star blocks giving massive multipliers.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📖 How To Play Manual",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(item.first, color = Color(0xFFA78BFA), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.second, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
