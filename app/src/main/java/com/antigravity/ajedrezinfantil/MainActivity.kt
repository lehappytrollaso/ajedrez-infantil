package com.antigravity.ajedrezinfantil

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tutorialManager: TutorialManager
    private var currentScreen: Screen = Screen.MAIN_MENU
    private var currentTheme: BoardTheme = BoardTheme.FANTASY

    enum class Screen {
        MAIN_MENU,
        TUTORIAL_LEVELS,
        TUTORIAL_PLAYER,
        MINIGAMES_MENU,
        PAWN_WARS,
        HUNGRY_KNIGHT,
        PRACTICE_GAME
    }

    enum class SparkyMood {
        NORMAL,
        THINKING,
        SURPRISED,
        CELEBRATING
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tutorialManager = TutorialManager(this)
        loadSavedTheme()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentScreen) {
                    Screen.MAIN_MENU -> finish()
                    Screen.TUTORIAL_LEVELS -> showMainMenu()
                    Screen.TUTORIAL_PLAYER -> showTutorialLevels()
                    Screen.MINIGAMES_MENU -> showMainMenu()
                    Screen.PAWN_WARS -> showMinigamesMenu()
                    Screen.HUNGRY_KNIGHT -> showMinigamesMenu()
                    Screen.PRACTICE_GAME -> showMainMenu()
                }
            }
        })

        showMainMenu()
    }

    // ==========================================
    // THEMES & PERSISTENCE
    // ==========================================
    private fun loadSavedTheme() {
        val prefs = getSharedPreferences("magic_chess_prefs", MODE_PRIVATE)
        val themeName = prefs.getString("theme_name", BoardTheme.FANTASY.name) ?: BoardTheme.FANTASY.name
        currentTheme = try {
            BoardTheme.valueOf(themeName)
        } catch (_: Exception) {
            BoardTheme.FANTASY
        }
    }

    private fun saveTheme(theme: BoardTheme) {
        currentTheme = theme
        getSharedPreferences("magic_chess_prefs", MODE_PRIVATE)
            .edit()
            .putString("theme_name", theme.name)
            .apply()
    }

    private fun showThemeSelectorDialog(onThemeChanged: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_theme_selector)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        fun applyTheme(theme: BoardTheme) {
            saveTheme(theme)
            SoundEffects.playPop(this)
            onThemeChanged()
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.btnThemeFantasy).setOnClickListener { applyTheme(BoardTheme.FANTASY) }
        dialog.findViewById<View>(R.id.btnThemeForest).setOnClickListener { applyTheme(BoardTheme.FOREST) }
        dialog.findViewById<View>(R.id.btnThemeIce).setOnClickListener { applyTheme(BoardTheme.ICE) }
        dialog.findViewById<View>(R.id.btnThemeCandy).setOnClickListener { applyTheme(BoardTheme.CANDY) }
        dialog.findViewById<View>(R.id.btnThemeClose).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setSparkyMood(imageView: ImageView?, mood: SparkyMood) {
        if (imageView == null) return
        val resId = when (mood) {
            SparkyMood.NORMAL -> R.drawable.ic_robot_sparky
            SparkyMood.THINKING -> R.drawable.ic_sparky_thinking
            SparkyMood.SURPRISED -> R.drawable.ic_sparky_surprised
            SparkyMood.CELEBRATING -> R.drawable.ic_sparky_celebrating
        }
        imageView.setImageResource(resId)
        imageView.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(120)
            .withEndAction {
                imageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
            }.start()
    }

    // ==========================================
    // 1. MAIN MENU
    // ==========================================
    private fun showMainMenu() {
        currentScreen = Screen.MAIN_MENU
        setContentView(R.layout.layout_main_menu)

        val tvStars = findViewById<TextView>(R.id.tvTotalStars)
        val btnSound = findViewById<ImageView>(R.id.btnSoundToggle)
        val btnTheme = findViewById<ImageView>(R.id.btnThemeToggle)
        val btnTutorial = findViewById<View>(R.id.btnMenuTutorial)
        val btnMinigames = findViewById<View>(R.id.btnMenuMinigames)
        val btnPlay = findViewById<View>(R.id.btnMenuPlay)

        val totalStars = tutorialManager.getTotalStars()
        val maxStars = TutorialCurriculum.levels.size * 3
        tvStars.text = "$totalStars / $maxStars"

        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
            if (!SoundEffects.isMuted) SoundEffects.playPop(this)
        }

        btnTheme.setOnClickListener {
            showThemeSelectorDialog {
                // Theme updated
            }
        }

        btnTutorial.setOnClickListener {
            SoundEffects.playPop(this)
            showTutorialLevels()
        }

        btnMinigames.setOnClickListener {
            SoundEffects.playPop(this)
            showMinigamesMenu()
        }

        btnPlay.setOnClickListener {
            SoundEffects.playPop(this)
            startPracticeGame()
        }
    }

    private fun updateSoundIcon(btn: ImageView) {
        btn.setImageResource(if (SoundEffects.isMuted) R.drawable.ic_sound_off else R.drawable.ic_sound_on)
    }

    // ==========================================
    // 2. TUTORIAL LEVELS LIST
    // ==========================================
    private fun showTutorialLevels() {
        currentScreen = Screen.TUTORIAL_LEVELS
        setContentView(R.layout.layout_tutorial_levels)

        findViewById<ImageView>(R.id.btnLevelsBack).setOnClickListener {
            SoundEffects.playPop()
            showMainMenu()
        }

        val container = findViewById<LinearLayout>(R.id.containerLevelsList)
        container.removeAllViews()

        for (lvl in TutorialCurriculum.levels) {
            val itemView = layoutInflater.inflate(R.layout.item_tutorial_level, container, false)

            val tvTitle = itemView.findViewById<TextView>(R.id.tvLevelTitle)
            val tvSubtitle = itemView.findViewById<TextView>(R.id.tvLevelSubtitle)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivLevelIcon)
            val ivStar1 = itemView.findViewById<ImageView>(R.id.ivStar1)
            val ivStar2 = itemView.findViewById<ImageView>(R.id.ivStar2)
            val ivStar3 = itemView.findViewById<ImageView>(R.id.ivStar3)

            tvTitle.text = "${lvl.title}: ${lvl.pieceName}"
            tvSubtitle.text = when (lvl.pieceType) {
                PieceType.PAWN -> "Pasito adelante y mordisquito diagonal"
                PieceType.ROOK -> "Vuelo recto como los muros de un castillo"
                PieceType.KNIGHT -> "¡Salto mágico en L sobre las piezas!"
                PieceType.BISHOP -> "Tobogán diagonal por su propio color"
                PieceType.QUEEN -> "La reina más poderosa de todo el reino"
                PieceType.KING -> "El rey que hay que proteger con cariño"
                else -> "¡El gran objetivo de la partida!"
            }
            ivIcon.setImageResource(lvl.iconRes)

            val stars = tutorialManager.getLevelStars(lvl.id)
            ivStar1.setImageResource(if (stars >= 1) R.drawable.ic_star_gold else R.drawable.ic_star_empty)
            ivStar2.setImageResource(if (stars >= 2) R.drawable.ic_star_gold else R.drawable.ic_star_empty)
            ivStar3.setImageResource(if (stars >= 3) R.drawable.ic_star_gold else R.drawable.ic_star_empty)

            itemView.setOnClickListener {
                SoundEffects.playPop()
                startTutorialLevel(lvl)
            }

            container.addView(itemView)
        }
    }

    // ==========================================
    // 3. INTERACTIVE TUTORIAL PLAYER
    // ==========================================
    private fun startTutorialLevel(level: TutorialLevel) {
        currentScreen = Screen.TUTORIAL_PLAYER
        setContentView(R.layout.layout_tutorial_player)

        val tvTitle = findViewById<TextView>(R.id.tvTutorialTitle)
        val tvStepCount = findViewById<TextView>(R.id.tvTutorialStepCount)
        val tvInstruction = findViewById<TextView>(R.id.tvTutorialInstruction)
        val boardView = findViewById<ChessBoardView>(R.id.chessBoardTutorial)
        boardView.currentTheme = currentTheme
        val containerTools = findViewById<View>(R.id.containerTutorialTools)
        val btnHint = findViewById<View>(R.id.btnTutorialHint)
        val btnRestart = findViewById<View>(R.id.btnTutorialRestart)
        val containerAction = findViewById<View>(R.id.containerTutorialAction)
        val btnAction = findViewById<Button>(R.id.btnTutorialAction)
        val btnBack = findViewById<ImageView>(R.id.btnTutorialBack)
        val btnSound = findViewById<ImageView>(R.id.btnTutorialSound)

        tvTitle.text = "${level.title}: ${level.pieceName}"
        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
        }

        var resetRunnable: Runnable? = null

        btnBack.setOnClickListener {
            resetRunnable?.let { boardView.removeCallbacks(it) }
            resetRunnable = null
            SoundEffects.playPop()
            showTutorialLevels()
        }

        var currentStepIndex = 0

        fun loadStep(stepIdx: Int) {
            resetRunnable?.let { boardView.removeCallbacks(it) }
            resetRunnable = null

            val step = level.steps[stepIdx]
            tvStepCount.text = "Paso ${stepIdx + 1} de ${level.steps.size}"
            tvInstruction.text = step.instruction

            val game = ChessGame()
            val remainingTargets = step.targetPositions.toMutableSet()

            fun resetStepBoard() {
                resetRunnable?.let { boardView.removeCallbacks(it) }
                resetRunnable = null

                game.clearBoard()
                game.setPiece(step.piecePos, Piece(step.pieceType, step.pieceColor))
                for ((pos, piece) in step.extraPieces) {
                    game.setPiece(pos, piece)
                }
                game.setTurn(step.pieceColor)

                boardView.setGame(game)
                boardView.tutorialTargetPositions = remainingTargets.toSet()
                boardView.tutorialArrows = emptyList()
                boardView.isInteractive = !step.isSchemeOnly
                boardView.invalidate()
            }

            resetStepBoard()

            if (step.isSchemeOnly) {
                // Generate all movement arrows to demonstrate
                val moves = game.generatePseudoMoves(step.piecePos)
                boardView.tutorialArrows = moves.map { Pair(it.from, it.to) }
                boardView.tutorialTargetPositions = emptySet()
                boardView.isInteractive = false
                boardView.invalidate()

                boardView.onSchemeTapListener = {
                    tvInstruction.text = "¡Mira las flechas! Cuando quieras jugar, pulsa el botón verde abajo 👇"
                    SoundEffects.playPop()
                    btnAction.animate().scaleX(1.05f).scaleY(1.05f).setDuration(120).withEndAction {
                        btnAction.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start()
                    }.start()
                }

                containerTools.visibility = View.GONE
                containerAction.visibility = View.VISIBLE
                btnAction.visibility = View.VISIBLE
                btnAction.text = "¡Entendido! Vamos a practicar 🚀"
                btnAction.setOnClickListener {
                    SoundEffects.playPop()
                    if (currentStepIndex + 1 < level.steps.size) {
                        currentStepIndex++
                        loadStep(currentStepIndex)
                    }
                }
            } else {
                // Interactive Practice Challenge!
                boardView.onSchemeTapListener = null
                boardView.onPendingResetTapListener = null
                boardView.onIllegalMoveListener = {
                    tvInstruction.text = "¡Toca o arrastra hacia los puntitos verdes o la estrella ⭐!"
                }
                boardView.onEnemyPieceTappedListener = {
                    tvInstruction.text = "¡Esa pieza es de Sparky! Mueve tu pieza blanca ✨"
                    SoundEffects.playPop()
                }

                containerTools.visibility = View.VISIBLE
                containerAction.visibility = View.GONE
                btnAction.visibility = View.GONE

                // Helper: Pista (Hint 💡)
                btnHint.setOnClickListener {
                    resetRunnable?.let { boardView.removeCallbacks(it) }
                    resetRunnable = null
                    resetStepBoard()
                    val target = remainingTargets.firstOrNull()
                    if (target != null) {
                        boardView.tutorialArrows = listOf(Pair(step.piecePos, target))
                        boardView.invalidate()
                        SoundEffects.playHint()
                        tvInstruction.text = "¡Pista! Mueve siguiendo la flecha dorada hacia la estrella ⭐"
                    }
                }

                // Helper: Reintentar (Retry 🔄)
                btnRestart.setOnClickListener {
                    SoundEffects.playPop()
                    remainingTargets.clear()
                    remainingTargets.addAll(step.targetPositions)
                    tvInstruction.text = step.instruction
                    resetStepBoard()
                }

                boardView.onUserMoveListener = { from, to ->
                    if (remainingTargets.contains(to)) {
                        // Target collected!
                        game.makeMove(Move(from, to))
                        game.setTurn(step.pieceColor) // Never leave turn in enemy color
                        remainingTargets.remove(to)
                        boardView.tutorialTargetPositions = remainingTargets.toSet()
                        boardView.tutorialArrows = emptyList()

                        if (remainingTargets.isEmpty()) {
                            // Level / step completed successfully
                            boardView.triggerConfetti()
                            SoundEffects.playStarCollect()
                            tvInstruction.text = step.successText

                            containerTools.visibility = View.GONE
                            containerAction.visibility = View.VISIBLE
                            btnAction.visibility = View.VISIBLE

                            if (currentStepIndex + 1 < level.steps.size) {
                                btnAction.text = "¡Siguiente reto! 🌟"
                                btnAction.setOnClickListener {
                                    SoundEffects.playPop()
                                    currentStepIndex++
                                    loadStep(currentStepIndex)
                                }
                            } else {
                                // Completed full level!
                                tutorialManager.setLevelStars(level.id, 3)
                                SoundEffects.playLevelComplete()
                                btnAction.text = "¡Nivel completado! Ganaste 3 ⭐⭐⭐"
                                btnAction.setOnClickListener {
                                    showCelebrationDialog(level)
                                }
                                val r = Runnable {
                                    showCelebrationDialog(level)
                                }
                                pendingCelebrationRunnable = r
                                boardView.postDelayed(r, 700)
                            }
                        } else {
                            // More targets to collect in this step
                            SoundEffects.playStarCollect()
                            tvInstruction.text = "¡Genial! ¡Atrapa la siguiente estrella! ⭐"
                            boardView.invalidate()
                        }
                    } else {
                        // Moved to a non-target valid square!
                        game.makeMove(Move(from, to))
                        game.setTurn(step.pieceColor)
                        boardView.tutorialArrows = emptyList()
                        SoundEffects.playInvalid()
                        tvInstruction.text = "¡Casi! Toca la estrella ⭐ o usa la Pista 💡"
                        boardView.invalidate()

                        boardView.isInteractive = false
                        // Allow immediate reset on tap without waiting 850ms
                        boardView.onPendingResetTapListener = {
                            resetRunnable?.let { boardView.removeCallbacks(it) }
                            resetRunnable = null
                            boardView.onPendingResetTapListener = null
                            resetStepBoard()
                            tvInstruction.text = step.instruction
                        }

                        val r = Runnable {
                            resetStepBoard()
                            tvInstruction.text = step.instruction
                            boardView.onPendingResetTapListener = null
                        }
                        resetRunnable = r
                        boardView.postDelayed(r, 850)
                    }
                }
            }
        }

        loadStep(currentStepIndex)
    }

    private var activeCelebrationDialog: Dialog? = null
    private var pendingCelebrationRunnable: Runnable? = null

    private fun showCelebrationDialog(level: TutorialLevel) {
        if (activeCelebrationDialog?.isShowing == true) return
        pendingCelebrationRunnable?.let {
            findViewById<View>(R.id.chessBoardTutorial)?.removeCallbacks(it)
        }
        pendingCelebrationRunnable = null

        val dialog = Dialog(this)
        activeCelebrationDialog = dialog
        dialog.setOnDismissListener {
            if (activeCelebrationDialog == dialog) {
                activeCelebrationDialog = null
            }
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_victory)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvVictoryTitle)
        val tvMsg = dialog.findViewById<TextView>(R.id.tvVictoryMessage)
        val btnPlayAgain = dialog.findViewById<Button>(R.id.btnVictoryPlayAgain)
        val btnMenu = dialog.findViewById<Button>(R.id.btnVictoryMenu)

        tvTitle.text = "¡Reto Superado! ⭐⭐⭐"
        tvMsg.text = "¡Has dominado el movimiento de ${level.pieceName} como una auténtica campeona! 👑"

        val nextLevel = TutorialCurriculum.levels.firstOrNull { it.id == level.id + 1 }
        if (nextLevel != null) {
            btnPlayAgain.text = "Siguiente pieza: ${nextLevel.pieceName} ➡️"
            btnPlayAgain.setOnClickListener {
                dialog.dismiss()
                startTutorialLevel(nextLevel)
            }
        } else {
            btnPlayAgain.text = "¡Jugar Partida Amistosa! 🎮"
            btnPlayAgain.setOnClickListener {
                dialog.dismiss()
                startPracticeGame()
            }
        }

        btnMenu.setOnClickListener {
            dialog.dismiss()
            showTutorialLevels()
        }

        dialog.show()
    }

    // ==========================================
    // 4. MINIGAMES (Guerra de Peones & El Festín del Caballo)
    // ==========================================
    private fun showMinigamesMenu() {
        currentScreen = Screen.MINIGAMES_MENU
        setContentView(R.layout.layout_minigames)

        val btnBack = findViewById<ImageView>(R.id.btnMinigamesBack)
        val btnTheme = findViewById<ImageView>(R.id.btnMinigamesTheme)
        val btnSound = findViewById<ImageView>(R.id.btnMinigamesSound)
        val btnPawnWars = findViewById<View>(R.id.btnMinigamePawnWars)
        val btnHungryKnight = findViewById<View>(R.id.btnMinigameHungryKnight)

        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
            if (!SoundEffects.isMuted) SoundEffects.playPop(this)
        }

        btnTheme.setOnClickListener {
            showThemeSelectorDialog {
                // Theme updated
            }
        }

        btnBack.setOnClickListener {
            SoundEffects.playPop(this)
            showMainMenu()
        }

        btnPawnWars.setOnClickListener {
            SoundEffects.playPop(this)
            startPawnWars()
        }

        btnHungryKnight.setOnClickListener {
            SoundEffects.playPop(this)
            startHungryKnight()
        }
    }

    private fun startPawnWars() {
        currentScreen = Screen.PAWN_WARS
        setContentView(R.layout.layout_pawn_wars)

        val boardView = findViewById<ChessBoardView>(R.id.chessBoardPawnWars)
        boardView.currentTheme = currentTheme
        val ivSparky = findViewById<ImageView>(R.id.ivPawnWarsSparky)
        val tvMessage = findViewById<TextView>(R.id.tvPawnWarsMessage)
        val btnBack = findViewById<ImageView>(R.id.btnPawnWarsBack)
        val btnRestart = findViewById<ImageView>(R.id.btnPawnWarsRestart)
        val btnTheme = findViewById<ImageView>(R.id.btnPawnWarsTheme)
        val btnSound = findViewById<ImageView>(R.id.btnPawnWarsSound)
        val btnHint = findViewById<View>(R.id.btnPawnWarsHint)
        val btnUndo = findViewById<View>(R.id.btnPawnWarsUndo)

        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
        }

        btnTheme.setOnClickListener {
            showThemeSelectorDialog {
                boardView.currentTheme = currentTheme
            }
        }

        var isComputerThinking = false
        var computerRunnable: Runnable? = null

        btnBack.setOnClickListener {
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false
            SoundEffects.playPop(this)
            showMinigamesMenu()
        }

        val game = ChessGame()
        game.setupPawnWars()
        boardView.setGame(game)
        boardView.isInteractive = true
        boardView.onSchemeTapListener = null
        boardView.onPendingResetTapListener = null

        fun checkPawnWarsEnd(): Boolean {
            val winner = game.checkPawnWarsWinner()
            if (winner != null) {
                boardView.isInteractive = false
                if (winner == PieceColor.WHITE) {
                    boardView.triggerConfetti()
                    SoundEffects.playVictory(this)
                    setSparkyMood(ivSparky, SparkyMood.CELEBRATING)
                    tvMessage.text = "¡¡¡CAMPEONA DE PEONES!!! 🏆 ¡Tu peón cruzó el tablero!"
                } else {
                    SoundEffects.playInvalid(this)
                    setSparkyMood(ivSparky, SparkyMood.SURPRISED)
                    tvMessage.text = "¡Sparky llegó primero! ¡Buen intento, la próxima ganas tú! ✨"
                }
                return true
            }
            return false
        }

        fun playSparkyPawnMove() {
            if (checkPawnWarsEnd()) return

            val blackMoves = game.getAllLegalMoves(PieceColor.BLACK)
            if (blackMoves.isEmpty()) {
                val whiteMoves = game.getAllLegalMoves(PieceColor.WHITE)
                if (whiteMoves.isEmpty()) {
                    checkPawnWarsEnd()
                } else {
                    game.setTurn(PieceColor.WHITE)
                    boardView.isInteractive = true
                    setSparkyMood(ivSparky, SparkyMood.SURPRISED)
                    tvMessage.text = "¡Sparky está bloqueado! ¡Sigue avanzando tú! 🚀"
                }
                return
            }

            isComputerThinking = true
            boardView.isInteractive = false
            setSparkyMood(ivSparky, SparkyMood.THINKING)
            tvMessage.text = "Sparky está pensando su jugada... 🤔"

            val r = Runnable {
                isComputerThinking = false
                val move = game.makeComputerMove(SparkyDifficulty.FRIEND)
                boardView.isInteractive = true
                if (move != null) {
                    SoundEffects.playMove(this)
                    setSparkyMood(ivSparky, SparkyMood.NORMAL)
                    tvMessage.text = "¡Turno tuyo! ¡Avanza hacia la gloria! ✨"
                    val ended = checkPawnWarsEnd()
                    if (!ended) {
                        if (game.getAllLegalMoves(PieceColor.WHITE).isEmpty()) {
                            if (game.getAllLegalMoves(PieceColor.BLACK).isNotEmpty()) {
                                tvMessage.text = "¡Tus peones están bloqueados! Sparky mueve de nuevo 🤖"
                                playSparkyPawnMove()
                            } else {
                                checkPawnWarsEnd()
                            }
                        }
                    }
                } else {
                    checkPawnWarsEnd()
                }
            }
            computerRunnable = r
            boardView.postDelayed(r, 600)
        }

        boardView.onUserMoveListener = { from, to ->
            val moved = game.makeMove(Move(from, to))
            if (moved) {
                SoundEffects.playMove(this)
                val isGameOver = checkPawnWarsEnd()
                if (!isGameOver) {
                    playSparkyPawnMove()
                }
            }
        }

        boardView.onEnemyPieceTappedListener = {
            setSparkyMood(ivSparky, SparkyMood.SURPRISED)
            tvMessage.text = "¡Ese peón es de Sparky! Toca tus peones blancos 🛡️"
            SoundEffects.playPop(this)
        }

        boardView.onIllegalMoveListener = {
            tvMessage.text = "¡Casilla no válida! Los peones avanzan 1 paso o comen en diagonal ♟️"
        }

        btnRestart.setOnClickListener {
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false
            SoundEffects.playPop(this)
            game.setupPawnWars()
            boardView.setGame(game)
            boardView.hintMove = null
            boardView.isInteractive = true
            setSparkyMood(ivSparky, SparkyMood.NORMAL)
            tvMessage.text = "¡Nueva Guerra de Peones! ¡El primero en llegar al final gana! 🛡️"
        }

        btnHint.setOnClickListener {
            if (isComputerThinking) return@setOnClickListener
            val hint = game.getBestHint()
            if (hint != null) {
                boardView.hintMove = hint
                boardView.invalidate()
                SoundEffects.playHint(this)
                tvMessage.text = "¡Pista! Avanza de ${hint.from.toChessNotation()} a ${hint.to.toChessNotation()} 💡"
            }
        }

        btnUndo.setOnClickListener {
            if (isComputerThinking) return@setOnClickListener
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false
            if (game.undo()) {
                if (game.turn == PieceColor.BLACK) game.undo()
                boardView.hintMove = null
                boardView.selectSquare(null)
                boardView.isInteractive = true
                boardView.invalidate()
                SoundEffects.playPop(this)
                tvMessage.text = "¡Jugada deshecha! Elige con calma 💭"
            }
        }
    }

    private fun startHungryKnight() {
        currentScreen = Screen.HUNGRY_KNIGHT
        setContentView(R.layout.layout_hungry_knight)

        val boardView = findViewById<ChessBoardView>(R.id.chessBoardHungryKnight)
        boardView.currentTheme = currentTheme
        val ivSparky = findViewById<ImageView>(R.id.ivHungryKnightSparky)
        val tvStage = findViewById<TextView>(R.id.tvHungryKnightStage)
        val tvMessage = findViewById<TextView>(R.id.tvHungryKnightMessage)
        val btnBack = findViewById<ImageView>(R.id.btnHungryKnightBack)
        val btnRestart = findViewById<ImageView>(R.id.btnHungryKnightRestart)
        val btnTheme = findViewById<ImageView>(R.id.btnHungryKnightTheme)
        val btnSound = findViewById<ImageView>(R.id.btnHungryKnightSound)
        val btnAction = findViewById<Button>(R.id.btnHungryKnightAction)
        val containerTools = findViewById<View>(R.id.containerHungryKnightTools)
        val btnHint = findViewById<View>(R.id.btnHungryKnightHint)
        val btnUndo = findViewById<View>(R.id.btnHungryKnightUndo)

        var currentStage = 1
        val maxStages = 3
        val game = ChessGame()
        val collectedStarsHistory = mutableListOf<Position?>()

        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
        }

        btnTheme.setOnClickListener {
            showThemeSelectorDialog {
                boardView.currentTheme = currentTheme
            }
        }

        btnBack.setOnClickListener {
            SoundEffects.playPop(this)
            showMinigamesMenu()
        }

        fun loadStage(stage: Int) {
            btnAction.visibility = View.GONE
            containerTools.visibility = View.VISIBLE
            collectedStarsHistory.clear()
            val level = game.setupHungryKnight(stage)
            tvStage.text = "Nivel $stage de $maxStages"
            boardView.setGame(game)
            boardView.tutorialTargetPositions = level.stars.toMutableSet()
            boardView.lavaPositions = level.lava.toMutableSet()
            boardView.isInteractive = true
            setSparkyMood(ivSparky, SparkyMood.NORMAL)
            tvMessage.text = when (stage) {
                1 -> "¡Salta en 'L' con el caballito! Come las 3 estrellas ⭐ ¡Esquiva la lava ardiente! 🔥"
                2 -> "¡Más estrellas y trampas de lava! Planea tus saltos mágicos 🦄✨"
                else -> "¡El festín final! ¡Come todas las estrellas del laberinto sin tocar el fuego! 🌟"
            }
        }

        boardView.onUserMoveListener = { from, to ->
            val moved = game.makeMove(Move(from, to))
            if (moved) {
                game.setTurn(PieceColor.WHITE) // Never leave turn in BLACK in solo puzzle
                if (boardView.tutorialTargetPositions.contains(to)) {
                    collectedStarsHistory.add(to)
                    boardView.tutorialTargetPositions = boardView.tutorialTargetPositions - to
                    boardView.invalidate()
                    SoundEffects.playStarCollect(this)
                    setSparkyMood(ivSparky, SparkyMood.SURPRISED)

                    val remaining = boardView.tutorialTargetPositions.size
                    if (remaining > 0) {
                        tvMessage.text = "¡Estrella comida! Yum yum ⭐ ¡Quedan $remaining!"
                    } else {
                        boardView.isInteractive = false
                        boardView.triggerConfetti()
                        SoundEffects.playVictory(this)
                        setSparkyMood(ivSparky, SparkyMood.CELEBRATING)
                        containerTools.visibility = View.GONE

                        if (currentStage < maxStages) {
                            tvMessage.text = "¡Nivel $currentStage completado! ¡Eres una saltadora experta! 🦄🎉"
                            btnAction.text = "¡Siguiente Nivel! ➡️"
                            btnAction.visibility = View.VISIBLE
                            btnAction.setOnClickListener {
                                SoundEffects.playPop(this)
                                currentStage++
                                loadStage(currentStage)
                            }
                        } else {
                            tvMessage.text = "¡FESTÍN COMPLETADO! ¡Has devorado todas las estrellas del reino! 👑🌟"
                            btnAction.text = "¡Jugar de Nuevo! 🔄"
                            btnAction.visibility = View.VISIBLE
                            btnAction.setOnClickListener {
                                SoundEffects.playPop(this)
                                currentStage = 1
                                loadStage(currentStage)
                            }
                        }
                    }
                } else {
                    collectedStarsHistory.add(null)
                    SoundEffects.playMove(this)
                    tvMessage.text = "¡Buen salto! Ahora busca la siguiente estrella ⭐"
                }
            }
        }

        boardView.onIllegalMoveListener = { targetPos ->
            SoundEffects.playInvalid(this)
            if (targetPos != null && boardView.lavaPositions.contains(targetPos)) {
                setSparkyMood(ivSparky, SparkyMood.SURPRISED)
                tvMessage.text = "¡CUIDADO! ¡Ahí hay lava ardiente! 🔥 Busca otra casilla segura"
            } else {
                tvMessage.text = "¡El caballo salta en 'L'! 2 pasos rectos y 1 al lado 🦄"
            }
        }

        btnHint.setOnClickListener {
            val hint = game.getHungryKnightHint(boardView.tutorialTargetPositions, boardView.lavaPositions)
            if (hint != null) {
                boardView.hintMove = hint
                boardView.invalidate()
                SoundEffects.playHint(this)
                tvMessage.text = "¡Pista! Salta hacia ${hint.to.toChessNotation()} para buscar la estrella ⭐"
            }
        }

        btnUndo.setOnClickListener {
            if (game.undo()) {
                game.setTurn(PieceColor.WHITE)
                val restored = if (collectedStarsHistory.isNotEmpty()) collectedStarsHistory.removeAt(collectedStarsHistory.size - 1) else null
                if (restored != null) {
                    boardView.tutorialTargetPositions = boardView.tutorialTargetPositions + restored
                }
                boardView.hintMove = null
                boardView.selectSquare(null)
                boardView.invalidate()
                SoundEffects.playPop(this)
                tvMessage.text = "¡Salto deshecho! Elige otro camino 🦄"
            }
        }

        btnRestart.setOnClickListener {
            SoundEffects.playPop(this)
            loadStage(currentStage)
        }

        loadStage(currentStage)
    }

    // ==========================================
    // 5. FRIENDLY PRACTICE GAME
    // ==========================================
    private fun startPracticeGame() {
        currentScreen = Screen.PRACTICE_GAME
        setContentView(R.layout.layout_practice_game)

        val boardView = findViewById<ChessBoardView>(R.id.chessBoardGame)
        boardView.currentTheme = currentTheme
        val ivSparky = findViewById<ImageView>(R.id.ivSparkyAvatar)
        val tvSparky = findViewById<TextView>(R.id.tvSparkyMessage)
        val tvStatus = findViewById<TextView>(R.id.tvPlayerStatus)
        val tvCapWhite = findViewById<TextView>(R.id.tvCapturedByWhite)
        val tvCapBlack = findViewById<TextView>(R.id.tvCapturedByBlack)
        val btnHint = findViewById<View>(R.id.btnGameHint)
        val btnUndo = findViewById<View>(R.id.btnGameUndo)
        val btnRestart = findViewById<ImageView>(R.id.btnGameRestart)
        val btnTheme = findViewById<ImageView>(R.id.btnGameTheme)
        val btnBack = findViewById<ImageView>(R.id.btnGameBack)
        val btnSound = findViewById<ImageView>(R.id.btnGameSound)

        val btnDiffToddler = findViewById<TextView>(R.id.btnDiffToddler)
        val btnDiffFriend = findViewById<TextView>(R.id.btnDiffFriend)
        val btnDiffChampion = findViewById<TextView>(R.id.btnDiffChampion)

        var currentDifficulty = SparkyDifficulty.TODDLER

        fun updateDifficultyUI() {
            btnDiffToddler.setBackgroundResource(if (currentDifficulty == SparkyDifficulty.TODDLER) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
            btnDiffToddler.setTextColor(if (currentDifficulty == SparkyDifficulty.TODDLER) Color.WHITE else Color.parseColor("#49454F"))

            btnDiffFriend.setBackgroundResource(if (currentDifficulty == SparkyDifficulty.FRIEND) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
            btnDiffFriend.setTextColor(if (currentDifficulty == SparkyDifficulty.FRIEND) Color.WHITE else Color.parseColor("#49454F"))

            btnDiffChampion.setBackgroundResource(if (currentDifficulty == SparkyDifficulty.CHAMPION) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
            btnDiffChampion.setTextColor(if (currentDifficulty == SparkyDifficulty.CHAMPION) Color.WHITE else Color.parseColor("#49454F"))
        }

        updateDifficultyUI()

        btnDiffToddler.setOnClickListener {
            currentDifficulty = SparkyDifficulty.TODDLER
            updateDifficultyUI()
            SoundEffects.playPop(this)
            setSparkyMood(ivSparky, SparkyMood.NORMAL)
            tvSparky.text = "¡Modo Aprendiz! Jugaremos muy suave y divertido 😊"
        }

        btnDiffFriend.setOnClickListener {
            currentDifficulty = SparkyDifficulty.FRIEND
            updateDifficultyUI()
            SoundEffects.playPop(this)
            setSparkyMood(ivSparky, SparkyMood.NORMAL)
            tvSparky.text = "¡Modo Amigo! Te ayudaré a aprender jugadas buenas ✨"
        }

        btnDiffChampion.setOnClickListener {
            currentDifficulty = SparkyDifficulty.CHAMPION
            updateDifficultyUI()
            SoundEffects.playPop(this)
            setSparkyMood(ivSparky, SparkyMood.SURPRISED)
            tvSparky.text = "¡Modo Campeona! ¡A ver si logras hacerme Jaque Mate! 🏆"
        }

        updateSoundIcon(btnSound)
        btnSound.setOnClickListener {
            SoundEffects.isMuted = !SoundEffects.isMuted
            updateSoundIcon(btnSound)
        }

        btnTheme.setOnClickListener {
            showThemeSelectorDialog {
                boardView.currentTheme = currentTheme
            }
        }

        var isComputerThinking = false
        var computerRunnable: Runnable? = null

        btnBack.setOnClickListener {
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false
            SoundEffects.playPop(this)
            showMainMenu()
        }

        val game = ChessGame()
        boardView.setGame(game)
        boardView.isInteractive = true
        boardView.onSchemeTapListener = null
        boardView.onPendingResetTapListener = null
        boardView.onEnemyPieceTappedListener = {
            setSparkyMood(ivSparky, SparkyMood.SURPRISED)
            tvSparky.text = "¡Esa pieza es de Sparky! Toca una de tus piezas blancas ✨"
            tvStatus.text = "¡Mueve tus piezas blancas! ✨"
            SoundEffects.playPop(this)
        }
        boardView.onIllegalMoveListener = {
            tvStatus.text = "¡Esa casilla no es válida! Toca los puntitos verdes o el escudo 🛡️"
        }

        fun updateCapturedDisplay() {
            fun pieceToEmoji(p: Piece): String = when (p.type) {
                PieceType.QUEEN -> "♛"
                PieceType.ROOK -> "♜"
                PieceType.BISHOP -> "♝"
                PieceType.KNIGHT -> "♞"
                PieceType.PAWN -> "♟"
                else -> ""
            }

            tvCapWhite.text = game.capturedByWhite.joinToString("") { pieceToEmoji(it) }
            tvCapBlack.text = game.capturedByBlack.joinToString("") { pieceToEmoji(it) }
        }

        fun checkGameStatus(): Boolean {
            boardView.updateCheckState()
            boardView.invalidate()

            if (game.isCheckmate(PieceColor.BLACK)) {
                boardView.isInteractive = false
                boardView.triggerConfetti()
                SoundEffects.playVictory(this)
                setSparkyMood(ivSparky, SparkyMood.CELEBRATING)
                tvSparky.text = "¡¡¡Enhorabuena!!! ¡Me has ganado! 🏆"
                tvStatus.text = "¡JAQUE MATE! ¡Eres la campeona! 🎉"
                showVictoryGameDialog(playerWon = true)
                return true
            }

            if (game.isCheckmate(PieceColor.WHITE)) {
                boardView.isInteractive = false
                setSparkyMood(ivSparky, SparkyMood.NORMAL)
                tvSparky.text = "¡Buen intento! ¡Casi me ganas! 🤝"
                tvStatus.text = "¡Jaque Mate! ¡La próxima vez lo lograrás! ✨"
                showVictoryGameDialog(playerWon = false)
                return true
            }

            if (game.isStalemate(PieceColor.WHITE) || game.isStalemate(PieceColor.BLACK)) {
                boardView.isInteractive = false
                setSparkyMood(ivSparky, SparkyMood.NORMAL)
                tvSparky.text = "¡Empate mágico! Muy bien jugado 🤝"
                tvStatus.text = "¡Rey ahogado, tablas! 🕊️"
                return true
            }

            if (game.isCheck(PieceColor.WHITE)) {
                tvStatus.text = "¡Cuidado! Tu rey está en Jaque ⚠️"
                tvStatus.setTextColor(Color.parseColor("#FF5252"))
                SoundEffects.playInvalid(this)
                setSparkyMood(ivSparky, SparkyMood.SURPRISED)
            } else {
                tvStatus.text = "¡Es tu turno! Toca una pieza para moverla ✨"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_mint))
            }

            return false
        }

        fun playComputerTurn() {
            isComputerThinking = true
            boardView.isInteractive = false
            setSparkyMood(ivSparky, SparkyMood.THINKING)
            tvSparky.text = "Sparky está pensando su jugada... 🤔"

            val r = Runnable {
                isComputerThinking = false
                val move = game.makeComputerMove(currentDifficulty)
                boardView.isInteractive = true
                if (move != null) {
                    SoundEffects.playMove(this)
                    updateCapturedDisplay()
                    setSparkyMood(ivSparky, SparkyMood.NORMAL)
                    val phrases = arrayOf(
                        "¡Listo! Ahora te toca a ti 🤖",
                        "¡Buena jugada! Veamos qué haces ahora ✨",
                        "¡Me gusta cómo juegas! Tu turno 💭",
                        "¡Cuidado con mis piezas traviesas! 😊"
                    )
                    tvSparky.text = phrases.random()
                    checkGameStatus()
                }
            }
            computerRunnable = r
            boardView.postDelayed(r, 650)
        }

        fun handlePlayerMove(from: Position, to: Position, promoteTo: PieceType = PieceType.QUEEN) {
            val destPiece = game.getPiece(to)
            val moved = game.makeMove(Move(from, to), promoteTo = promoteTo)
            if (moved) {
                if (destPiece != null) {
                    SoundEffects.playCapture(this)
                    setSparkyMood(ivSparky, SparkyMood.SURPRISED)
                } else {
                    SoundEffects.playMove(this)
                }
                updateCapturedDisplay()
                val gameOver = checkGameStatus()
                if (!gameOver) {
                    playComputerTurn()
                }
            }
        }

        boardView.onUserMoveListener = { from, to ->
            val piece = game.getPiece(from)
            if (piece != null && piece.type == PieceType.PAWN && to.row == 0) {
                showPromotionDialog { chosenType ->
                    handlePlayerMove(from, to, chosenType)
                }
            } else {
                handlePlayerMove(from, to)
            }
        }

        // Pista (Hint)
        btnHint.setOnClickListener {
            if (isComputerThinking) return@setOnClickListener
            if (game.turn == PieceColor.WHITE) {
                val hint = game.getBestHint()
                if (hint != null) {
                    boardView.hintMove = hint
                    boardView.invalidate()
                    SoundEffects.playHint(this)
                    tvSparky.text = "¡Pista! Mueve de ${hint.from.toChessNotation()} a ${hint.to.toChessNotation()} 💡"
                }
            }
        }

        // Deshacer (Undo)
        btnUndo.setOnClickListener {
            if (isComputerThinking) return@setOnClickListener
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false

            if (game.undo()) {
                if (game.turn == PieceColor.BLACK) {
                    game.undo()
                }
                boardView.hintMove = null
                boardView.selectSquare(null)
                boardView.updateCheckState()
                boardView.isInteractive = true
                boardView.invalidate()
                updateCapturedDisplay()
                SoundEffects.playPop(this)
                setSparkyMood(ivSparky, SparkyMood.NORMAL)
                tvSparky.text = "¡Jugada deshecha! Tómate tu tiempo 💭"
                checkGameStatus()
            }
        }

        // Reiniciar (Restart)
        btnRestart.setOnClickListener {
            computerRunnable?.let { boardView.removeCallbacks(it) }
            computerRunnable = null
            isComputerThinking = false
            SoundEffects.playPop(this)
            game.resetToStandard()
            boardView.setGame(game)
            boardView.hintMove = null
            boardView.isInteractive = true
            updateCapturedDisplay()
            setSparkyMood(ivSparky, SparkyMood.NORMAL)
            tvSparky.text = "¡Partida nueva! ¡A divertirse! 🎉"
            tvStatus.text = "¡Es tu turno! Toca una pieza para moverla"
        }
    }


    private fun showPromotionDialog(onChosen: (PieceType) -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pawn_promotion)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialog.findViewById<View>(R.id.btnPromoQueen).setOnClickListener {
            SoundEffects.playPop()
            dialog.dismiss()
            onChosen(PieceType.QUEEN)
        }
        dialog.findViewById<View>(R.id.btnPromoRook).setOnClickListener {
            SoundEffects.playPop()
            dialog.dismiss()
            onChosen(PieceType.ROOK)
        }
        dialog.findViewById<View>(R.id.btnPromoBishop).setOnClickListener {
            SoundEffects.playPop()
            dialog.dismiss()
            onChosen(PieceType.BISHOP)
        }
        dialog.findViewById<View>(R.id.btnPromoKnight).setOnClickListener {
            SoundEffects.playPop()
            dialog.dismiss()
            onChosen(PieceType.KNIGHT)
        }

        dialog.show()
    }

    private var activeVictoryGameDialog: Dialog? = null

    private fun showVictoryGameDialog(playerWon: Boolean) {
        if (activeVictoryGameDialog?.isShowing == true) return
        val dialog = Dialog(this)
        activeVictoryGameDialog = dialog
        dialog.setOnDismissListener {
            if (activeVictoryGameDialog == dialog) {
                activeVictoryGameDialog = null
            }
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_victory)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvTitle = dialog.findViewById<TextView>(R.id.tvVictoryTitle)
        val tvMsg = dialog.findViewById<TextView>(R.id.tvVictoryMessage)
        val btnPlayAgain = dialog.findViewById<Button>(R.id.btnVictoryPlayAgain)
        val btnMenu = dialog.findViewById<Button>(R.id.btnVictoryMenu)

        if (playerWon) {
            tvTitle.text = "¡¡¡CAMPEONA MÁGICA!!! 🏆👑"
            tvMsg.text = "¡Has derrotado a Sparky con un Jaque Mate de ensueño! ¡Estás lista para cualquier partida! ✨"
        } else {
            tvTitle.text = "¡Partida muy reñida! 🤝"
            tvMsg.text = "¡Has jugado genial! Cada partida te hace más lista y fuerte en el ajedrez. ¡A por la revancha! 🌟"
        }

        btnPlayAgain.setOnClickListener {
            dialog.dismiss()
            startPracticeGame()
        }

        btnMenu.setOnClickListener {
            dialog.dismiss()
            showMainMenu()
        }

        dialog.show()
    }
}
