package com.antigravity.ajedrezinfantil

import org.junit.Assert.*
import org.junit.Test

class ChessEngineTest {

    @Test
    fun testInitialBoardSetup() {
        val game = ChessGame()
        assertEquals(PieceColor.WHITE, game.turn)

        // White pieces
        assertEquals(Piece(PieceType.KING, PieceColor.WHITE), game.getPiece(Position(7, 4)))
        assertEquals(Piece(PieceType.QUEEN, PieceColor.WHITE), game.getPiece(Position(7, 3)))
        assertEquals(Piece(PieceType.ROOK, PieceColor.WHITE), game.getPiece(Position(7, 0)))
        assertEquals(Piece(PieceType.KNIGHT, PieceColor.WHITE), game.getPiece(Position(7, 1)))
        assertEquals(Piece(PieceType.BISHOP, PieceColor.WHITE), game.getPiece(Position(7, 2)))

        // Black pieces
        assertEquals(Piece(PieceType.KING, PieceColor.BLACK), game.getPiece(Position(0, 4)))
        assertEquals(Piece(PieceType.QUEEN, PieceColor.BLACK), game.getPiece(Position(0, 3)))
        assertEquals(Piece(PieceType.ROOK, PieceColor.BLACK), game.getPiece(Position(0, 7)))

        // Pawns
        for (c in 0..7) {
            assertEquals(Piece(PieceType.PAWN, PieceColor.WHITE), game.getPiece(Position(6, c)))
            assertEquals(Piece(PieceType.PAWN, PieceColor.BLACK), game.getPiece(Position(1, c)))
        }

        assertFalse(game.isCheck(PieceColor.WHITE))
        assertFalse(game.isCheck(PieceColor.BLACK))
    }

    @Test
    fun testPawnMoves() {
        val game = ChessGame()
        // White Pawn e2 (row 6, col 4) can move to e3 (row 5, col 4) or e4 (row 4, col 4)
        val legalMoves = game.getLegalMoves(Position(6, 4))
        assertEquals(2, legalMoves.size)
        assertTrue(legalMoves.any { it.to == Position(5, 4) })
        assertTrue(legalMoves.any { it.to == Position(4, 4) })

        // Move to e4
        val success = game.makeMove(Move(Position(6, 4), Position(4, 4)))
        assertTrue(success)
        assertEquals(PieceColor.BLACK, game.turn)
        assertNull(game.getPiece(Position(6, 4)))
        assertEquals(Piece(PieceType.PAWN, PieceColor.WHITE), game.getPiece(Position(4, 4)))
    }

    @Test
    fun testKnightMovesAndJumping() {
        val game = ChessGame()
        // Knight at b1 (row 7, col 1) can jump over pawns to a3 (row 5, col 0) or c3 (row 5, col 2)
        val knightMoves = game.getLegalMoves(Position(7, 1))
        assertEquals(2, knightMoves.size)
        assertTrue(knightMoves.any { it.to == Position(5, 0) })
        assertTrue(knightMoves.any { it.to == Position(5, 2) })

        // Execute jump
        val success = game.makeMove(Move(Position(7, 1), Position(5, 2)))
        assertTrue(success)
        assertEquals(Piece(PieceType.KNIGHT, PieceColor.WHITE), game.getPiece(Position(5, 2)))
    }

    @Test
    fun testCheckmateDetectionFoolsmate() {
        val game = ChessGame()
        // Fool's mate:
        // 1. f3 e5 2. g4 Qh4#
        assertTrue(game.makeMove(Move(Position(6, 5), Position(5, 5)))) // f3
        assertTrue(game.makeMove(Move(Position(1, 4), Position(3, 4)))) // e5
        assertTrue(game.makeMove(Move(Position(6, 6), Position(4, 6)))) // g4
        assertTrue(game.makeMove(Move(Position(0, 3), Position(4, 7)))) // Qh4#

        assertTrue(game.isCheck(PieceColor.WHITE))
        assertTrue(game.isCheckmate(PieceColor.WHITE))
        assertFalse(game.isCheckmate(PieceColor.BLACK))
    }

    @Test
    fun testUndoFunctionality() {
        val game = ChessGame()
        val origPiece = game.getPiece(Position(6, 4))
        game.makeMove(Move(Position(6, 4), Position(4, 4)))
        assertEquals(PieceColor.BLACK, game.turn)

        val undone = game.undo()
        assertTrue(undone)
        assertEquals(PieceColor.WHITE, game.turn)
        assertEquals(origPiece, game.getPiece(Position(6, 4)))
        assertNull(game.getPiece(Position(4, 4)))
    }

    @Test
    fun testKidAIGeneratesLegalMove() {
        val game = ChessGame()
        // Player moves e4
        game.makeMove(Move(Position(6, 4), Position(4, 4)))

        // Kid AI moves for Black
        val aiMove = game.makeComputerMove()
        assertNotNull(aiMove)
        assertEquals(PieceColor.WHITE, game.turn)
    }

    @Test
    fun testTutorialCurriculumIntegrity() {
        assertEquals(7, TutorialCurriculum.levels.size)

        for (lvl in TutorialCurriculum.levels) {
            assertTrue(lvl.steps.isNotEmpty())
            for (step in lvl.steps) {
                assertTrue(step.piecePos.isValid())
                for (target in step.targetPositions) {
                    assertTrue(target.isValid())
                }
                for ((pos, piece) in step.extraPieces) {
                    assertTrue(pos.isValid())
                    assertNotNull(piece)
                }
            }
        }
    }
}
