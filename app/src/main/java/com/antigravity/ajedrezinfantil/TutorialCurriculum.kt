package com.antigravity.ajedrezinfantil

import android.content.Context
import android.content.SharedPreferences

data class TutorialStep(
    val title: String,
    val instruction: String,
    val pieceType: PieceType,
    val pieceColor: PieceColor = PieceColor.WHITE,
    val piecePos: Position,
    val extraPieces: Map<Position, Piece> = emptyMap(),
    val targetPositions: Set<Position> = emptySet(),
    val isSchemeOnly: Boolean = false,
    val successText: String = "¡Muy bien! ⭐⭐⭐"
)

data class TutorialLevel(
    val id: Int,
    val title: String,
    val pieceName: String,
    val pieceType: PieceType,
    val iconRes: Int,
    val colorRes: Int,
    val steps: List<TutorialStep>
)

object TutorialCurriculum {
    val levels = listOf(
        // 1. EL PEÓN
        TutorialLevel(
            id = 1,
            title = "Nivel 1",
            pieceName = "El Peoncito",
            pieceType = PieceType.PAWN,
            iconRes = R.drawable.piece_pawn_white,
            colorRes = R.color.primary,
            steps = listOf(
                TutorialStep(
                    title = "Movimiento del Peón",
                    instruction = "El peón da 1 pasito siempre hacia adelante.",
                    pieceType = PieceType.PAWN,
                    piecePos = Position(5, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "¡Tu turno! Da 1 pasito",
                    instruction = "Toca el peón y muévelo hacia la estrella ⭐",
                    pieceType = PieceType.PAWN,
                    piecePos = Position(5, 4),
                    targetPositions = setOf(Position(4, 4)),
                    successText = "¡Excelente pasito! ⭐"
                ),
                TutorialStep(
                    title = "¡El gran salto inicial!",
                    instruction = "Desde su casilla de salida, ¡puede avanzar 2 casillas!",
                    pieceType = PieceType.PAWN,
                    piecePos = Position(6, 3),
                    targetPositions = setOf(Position(4, 3)),
                    successText = "¡Super salto de 2 casillas! ⭐⭐"
                ),
                TutorialStep(
                    title = "¡Captura en diagonal!",
                    instruction = "El peón come en diagonal, 1 casilla a un ladito.",
                    pieceType = PieceType.PAWN,
                    piecePos = Position(5, 4),
                    extraPieces = mapOf(Position(4, 5) to Piece(PieceType.PAWN, PieceColor.BLACK)),
                    targetPositions = setOf(Position(4, 5)),
                    successText = "¡Captura diagonal perfecta! ⭐⭐⭐"
                )
            )
        ),

        // 2. LA TORRE
        TutorialLevel(
            id = 2,
            title = "Nivel 2",
            pieceName = "La Torre Fuerte",
            pieceType = PieceType.ROOK,
            iconRes = R.drawable.piece_rook_white,
            colorRes = R.color.accent_mint,
            steps = listOf(
                TutorialStep(
                    title = "Movimiento de la Torre",
                    instruction = "La torre vuela en líneas rectas: arriba, abajo, izquierda y derecha.",
                    pieceType = PieceType.ROOK,
                    piecePos = Position(4, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "Línea recta al frente",
                    instruction = "Mueve la torre recta hacia arriba para atrapar la estrella ⭐",
                    pieceType = PieceType.ROOK,
                    piecePos = Position(6, 3),
                    targetPositions = setOf(Position(2, 3)),
                    successText = "¡Qué gran velocidad recta! ⭐"
                ),
                TutorialStep(
                    title = "Camino lateral",
                    instruction = "Desliza la torre a la derecha hasta la estrella ⭐",
                    pieceType = PieceType.ROOK,
                    piecePos = Position(3, 1),
                    targetPositions = setOf(Position(3, 6)),
                    successText = "¡La torre domina el castillo! ⭐⭐⭐"
                )
            )
        ),

        // 3. EL CABALLO
        TutorialLevel(
            id = 3,
            title = "Nivel 3",
            pieceName = "El Caballito Saltarín",
            pieceType = PieceType.KNIGHT,
            iconRes = R.drawable.piece_knight_white,
            colorRes = R.color.accent_gold,
            steps = listOf(
                TutorialStep(
                    title = "El Salto Mágico en L",
                    instruction = "El caballo da 2 pasos rectos y 1 al lado, dibujando una letra L.",
                    pieceType = PieceType.KNIGHT,
                    piecePos = Position(4, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "¡Salta a la estrella!",
                    instruction = "Toca el caballito y salta en L hasta la estrella ⭐",
                    pieceType = PieceType.KNIGHT,
                    piecePos = Position(5, 4),
                    targetPositions = setOf(Position(3, 5)),
                    successText = "¡Saltito perfecto! ⭐"
                ),
                TutorialStep(
                    title = "¡Saltando por encima!",
                    instruction = "¡El caballo puede saltar por encima de otras piezas!",
                    pieceType = PieceType.KNIGHT,
                    piecePos = Position(6, 4),
                    extraPieces = mapOf(
                        Position(5, 4) to Piece(PieceType.PAWN, PieceColor.WHITE),
                        Position(5, 3) to Piece(PieceType.PAWN, PieceColor.WHITE),
                        Position(5, 5) to Piece(PieceType.PAWN, PieceColor.WHITE)
                    ),
                    targetPositions = setOf(Position(4, 5)),
                    successText = "¡Increíble salto sobre la barrera! ⭐⭐⭐"
                )
            )
        ),

        // 4. EL ALFIL
        TutorialLevel(
            id = 4,
            title = "Nivel 4",
            pieceName = "El Alfil del Bosque",
            pieceType = PieceType.BISHOP,
            iconRes = R.drawable.piece_bishop_white,
            colorRes = R.color.accent_pink,
            steps = listOf(
                TutorialStep(
                    title = "El Tobogán Diagonal",
                    instruction = "El alfil se desliza en diagonal. ¡Siempre por casillas de su mismo color!",
                    pieceType = PieceType.BISHOP,
                    piecePos = Position(4, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "Deslízate en diagonal",
                    instruction = "Usa el tobogán diagonal para atrapar la estrella ⭐",
                    pieceType = PieceType.BISHOP,
                    piecePos = Position(6, 2),
                    targetPositions = setOf(Position(3, 5)),
                    successText = "¡Deslizamiento perfecto! ⭐"
                ),
                TutorialStep(
                    title = "Cruzando el tablero",
                    instruction = "Deslízate al otro lado hasta la estrella ⭐",
                    pieceType = PieceType.BISHOP,
                    piecePos = Position(2, 6),
                    targetPositions = setOf(Position(6, 2)),
                    successText = "¡El alfil no sale de su sendero! ⭐⭐⭐"
                )
            )
        ),

        // 5. LA REINA
        TutorialLevel(
            id = 5,
            title = "Nivel 5",
            pieceName = "La Reina Valiente",
            pieceType = PieceType.QUEEN,
            iconRes = R.drawable.piece_queen_white,
            colorRes = R.color.primary_dark,
            steps = listOf(
                TutorialStep(
                    title = "¡El Superpoder de la Reina!",
                    instruction = "La reina es Torre + Alfil: ¡se mueve recto y en diagonal hacia donde quiera!",
                    pieceType = PieceType.QUEEN,
                    piecePos = Position(4, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "Vuelo libre",
                    instruction = "Mueve la reina en diagonal hacia la estrella lejana ⭐",
                    pieceType = PieceType.QUEEN,
                    piecePos = Position(7, 3),
                    targetPositions = setOf(Position(2, 3)),
                    successText = "¡Poder real en acción! ⭐"
                ),
                TutorialStep(
                    title = "¡A rescatar el reino!",
                    instruction = "Atrapa la estrella en la esquina con la reina ⭐",
                    pieceType = PieceType.QUEEN,
                    piecePos = Position(5, 5),
                    targetPositions = setOf(Position(1, 1)),
                    successText = "¡La reina es imparable! ⭐⭐⭐"
                )
            )
        ),

        // 6. EL REY
        TutorialLevel(
            id = 6,
            title = "Nivel 6",
            pieceName = "El Rey Sabio",
            pieceType = PieceType.KING,
            iconRes = R.drawable.piece_king_white,
            colorRes = R.color.accent_gold,
            steps = listOf(
                TutorialStep(
                    title = "El Rey y su Pasito",
                    instruction = "El rey se mueve 1 sola casilla hacia cualquier lado. ¡Hay que cuidarlo mucho!",
                    pieceType = PieceType.KING,
                    piecePos = Position(4, 4),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "Un paso cuidadoso",
                    instruction = "Da un pasito seguro con el rey hacia la corona ⭐",
                    pieceType = PieceType.KING,
                    piecePos = Position(5, 4),
                    targetPositions = setOf(Position(4, 4)),
                    successText = "¡El rey camina seguro! ⭐"
                ),
                TutorialStep(
                    title = "Paso en diagonal",
                    instruction = "Mueve el rey 1 casilla en diagonal hacia la estrella ⭐",
                    pieceType = PieceType.KING,
                    piecePos = Position(4, 3),
                    targetPositions = setOf(Position(3, 4)),
                    successText = "¡Rey protegido y a salvo! ⭐⭐⭐"
                )
            )
        ),

        // 7. JAQUE Y JAQUE MATE
        TutorialLevel(
            id = 7,
            title = "Nivel 7",
            pieceName = "¡Jaque Mate!",
            pieceType = PieceType.QUEEN,
            iconRes = R.drawable.ic_trophy,
            colorRes = R.color.accent_pink,
            steps = listOf(
                TutorialStep(
                    title = "¿Qué es Jaque?",
                    instruction = "¡Jaque es un aviso! Significa que el rey rival está en peligro.",
                    pieceType = PieceType.ROOK,
                    piecePos = Position(4, 1),
                    extraPieces = mapOf(Position(4, 6) to Piece(PieceType.KING, PieceColor.BLACK)),
                    isSchemeOnly = true
                ),
                TutorialStep(
                    title = "¡Da Jaque al Rey!",
                    instruction = "Mueve la torre a la fila del rey negro para darle Jaque 🎯",
                    pieceType = PieceType.ROOK,
                    piecePos = Position(7, 3),
                    extraPieces = mapOf(Position(2, 6) to Piece(PieceType.KING, PieceColor.BLACK)),
                    targetPositions = setOf(Position(2, 3)),
                    successText = "¡JAQUE! El rey está avisado 🎯"
                ),
                TutorialStep(
                    title = "¡El Gran Jaque Mate!",
                    instruction = "Mueve la dama justo delante del rey para darle ¡Jaque Mate y ganar la copa! 🏆",
                    pieceType = PieceType.QUEEN,
                    piecePos = Position(6, 4),
                    extraPieces = mapOf(
                        Position(0, 4) to Piece(PieceType.KING, PieceColor.BLACK),
                        Position(2, 4) to Piece(PieceType.KING, PieceColor.WHITE)
                    ),
                    targetPositions = setOf(Position(1, 4)),
                    successText = "¡¡¡JAQUE MATE, CAMPEONA!!! 🏆🎉✨"
                )
            )
        )
    )
}

class TutorialManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ajedrez_infantil_prefs", Context.MODE_PRIVATE)

    fun getLevelStars(levelId: Int): Int {
        return prefs.getInt("level_stars_$levelId", 0)
    }

    fun setLevelStars(levelId: Int, stars: Int) {
        val current = getLevelStars(levelId)
        if (stars > current) {
            prefs.edit().putInt("level_stars_$levelId", stars).apply()
        }
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        return getLevelStars(levelId - 1) > 0
    }

    fun getTotalStars(): Int {
        var sum = 0
        for (lvl in TutorialCurriculum.levels) {
            sum += getLevelStars(lvl.id)
        }
        return sum
    }
}
