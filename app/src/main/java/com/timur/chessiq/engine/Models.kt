package com.timur.chessiq.engine

enum class Side { WHITE, BLACK; fun other() = if (this == WHITE) BLACK else WHITE }
enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

data class Piece(val type: PieceType, val side: Side)

data class Square(val file: Int, val rank: Int) {
    val idx get() = rank * 8 + file
    companion object { fun fromIdx(i: Int) = Square(i % 8, i / 8) }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
    val isCapture: Boolean = false,
    val isEnPassant: Boolean = false,
    val isCastleKing: Boolean = false,
    val isCastleQueen: Boolean = false
)

fun Piece.unicode(): String = when (type) {
    PieceType.KING   -> if (side == Side.WHITE) "♔" else "♚"
    PieceType.QUEEN  -> if (side == Side.WHITE) "♕" else "♛"
    PieceType.ROOK   -> if (side == Side.WHITE) "♖" else "♜"
    PieceType.BISHOP -> if (side == Side.WHITE) "♗" else "♝"
    PieceType.KNIGHT -> if (side == Side.WHITE) "♘" else "♞"
    PieceType.PAWN   -> if (side == Side.WHITE) "♙" else "♟"
}
