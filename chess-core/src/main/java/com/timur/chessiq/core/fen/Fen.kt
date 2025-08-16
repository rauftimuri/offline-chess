package com.timur.chessiq.core.fen

import com.timur.chessiq.core.model.*

private val charToPiece = mapOf(
    'P' to Piece(PieceType.P, Color.WHITE), 'N' to Piece(PieceType.N, Color.WHITE),
    'B' to Piece(PieceType.B, Color.WHITE), 'R' to Piece(PieceType.R, Color.WHITE),
    'Q' to Piece(PieceType.Q, Color.WHITE), 'K' to Piece(PieceType.K, Color.WHITE),
    'p' to Piece(PieceType.P, Color.BLACK), 'n' to Piece(PieceType.N, Color.BLACK),
    'b' to Piece(PieceType.B, Color.BLACK), 'r' to Piece(PieceType.R, Color.BLACK),
    'q' to Piece(PieceType.Q, Color.BLACK), 'k' to Piece(PieceType.K, Color.BLACK)
)

private fun pieceToChar(p: Piece) = when (p.color) {
    Color.WHITE -> when (p.type) { PieceType.P->'P'; PieceType.N->'N'; PieceType.B->'B'; PieceType.R->'R'; PieceType.Q->'Q'; PieceType.K->'K' }
    Color.BLACK -> when (p.type) { PieceType.P->'p'; PieceType.N->'n'; PieceType.B->'b'; PieceType.R->'r'; PieceType.Q->'q'; PieceType.K->'k' }
}

private fun idx(file: Int, rank: Int) = rank * 8 + file // 0..63, a1=(0,0)

fun fenToState(fen: String): GameState {
    val parts = fen.trim().split(Regex("\\s+"))
    require(parts.size >= 4) { "Invalid FEN: $fen" }

    val board = arrayOfNulls<Piece>(64)
    // 1) Tahta
    val ranks = parts[0].split("/")
    require(ranks.size == 8) { "Invalid board in FEN" }
    for ((rankFromTop, row) in ranks.withIndex()) {
        var file = 0
        for (c in row) {
            when {
                c.isDigit() -> file += c.digitToInt()
                c in charToPiece -> {
                    val r = 7 - rankFromTop         // FEN üstten başlar, iç temsil alttan (a1=rank0)
                    val i = idx(file, r)
                    board[i] = charToPiece.getValue(c)
                    file++
                }
                c == '/' -> {} // yok
                else -> error("Bad FEN char: $c")
            }
        }
        require(file == 8) { "Row width != 8 in FEN" }
    }
    // 2) Sıra
    val sideToMove = when (parts[1]) {
        "w" -> Color.WHITE
        "b" -> Color.BLACK
        else -> error("Bad side to move")
    }
    // 3) Rok hakları
    val crStr = parts[2]
    val castling = CastlingRights(
        whiteKingSide  = 'K' in crStr,
        whiteQueenSide = 'Q' in crStr,
        blackKingSide  = 'k' in crStr,
        blackQueenSide = 'q' in crStr
    )
    // 4) En passant
    val ep = parts[3].let { sq ->
        if (sq == "-" ) null
        else {
            require(sq.length == 2) { "Bad en passant square" }
            val file = sq[0] - 'a'
            val rank = (sq[1] - '1')
            Square(file, rank)
        }
    }
    val half = parts.getOrNull(4)?.toIntOrNull() ?: 0
    val full = parts.getOrNull(5)?.toIntOrNull() ?: 1

    return GameState(board = board, sideToMove = sideToMove, castling = castling, enPassant = ep, halfmoveClock = half, fullmoveNumber = full)
}

fun stateToFen(state: GameState): String {
    // 1) Tahta
    val sb = StringBuilder()
    for (rankFromTop in 7 downTo 0) {
        var empty = 0
        for (file in 0..7) {
            val p = state.board[idx(file, rankFromTop)]
            if (p == null) empty++ else {
                if (empty > 0) { sb.append(empty); empty = 0 }
                sb.append(pieceToChar(p))
            }
        }
        if (empty > 0) sb.append(empty)
        if (rankFromTop != 0) sb.append('/')
    }
    // 2) Sıra
    val side = if (state.sideToMove == Color.WHITE) "w" else "b"
    // 3) Rok
    val cr = buildString {
        if (state.castling.whiteKingSide) append('K')
        if (state.castling.whiteQueenSide) append('Q')
        if (state.castling.blackKingSide) append('k')
        if (state.castling.blackQueenSide) append('q')
        if (isEmpty()) append('-')
    }
    // 4) En passant
    val ep = state.enPassant?.let { "${'a' + it.file}${'1' + it.rank}" } ?: "-"
    // 5–6) Sayaçlar
    return "$sb $side $cr $ep ${state.halfmoveClock} ${state.fullmoveNumber}"
}

// Yardımcı: başlangıç FEN’i
val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
