package com.timur.chessiq.core.model

enum class Color { WHITE, BLACK }
enum class PieceType { P, N, B, R, Q, K }

data class Piece(val type: PieceType, val color: Color)

data class Square(val file: Int, val rank: Int) {
    init { require(file in 0..7 && rank in 0..7) { "Square out of board: $file,$rank" } }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null
)

data class CastlingRights(
    val whiteKingSide: Boolean = true,
    val whiteQueenSide: Boolean = true,
    val blackKingSide: Boolean = true,
    val blackQueenSide: Boolean = true
)

data class GameState(
    val board: Array<Piece?>,         // 64 kare: 0..63
    val sideToMove: Color,
    val castling: CastlingRights,
    val enPassant: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1
) {
    init { require(board.size == 64) { "Board must be 64 squares" } }
}
