package com.timur.chessiq.engine

class Board private constructor(
    private val squares: Array<Piece?>,
    val sideToMove: Side,
    val whiteCastleK: Boolean,
    val whiteCastleQ: Boolean,
    val blackCastleK: Boolean,
    val blackCastleQ: Boolean,
    val enPassant: Square?
) {
    fun pieceAt(sq: Square) = squares[sq.idx]
    private fun setPiece(sq: Square, p: Piece?) { squares[sq.idx] = p }
    fun copy() = Board(squares.copyOf(), sideToMove, whiteCastleK, whiteCastleQ, blackCastleK, blackCastleQ, enPassant)

    fun make(move: Move): Board {
        val b = this.copyMutable()
        if (move.isCastleKing || move.isCastleQueen) {
            val rank = if (sideToMove == Side.WHITE) 7 else 0
            val kingFrom = Square(4, rank)
            val kingTo   = if (move.isCastleKing) Square(6, rank) else Square(2, rank)
            val rookFrom = if (move.isCastleKing) Square(7, rank) else Square(0, rank)
            val rookTo   = if (move.isCastleKing) Square(5, rank) else Square(3, rank)
            b.squares[kingTo.idx] = b.squares[kingFrom.idx]; b.squares[kingFrom.idx] = null
            b.squares[rookTo.idx] = b.squares[rookFrom.idx]; b.squares[rookFrom.idx] = null
        } else {
            val moving = b.squares[move.from.idx]
            b.squares[move.from.idx] = null
            if (move.isEnPassant) {
                val dir = if (sideToMove == Side.WHITE) 1 else -1
                val capSq = Square(move.to.file, move.to.rank + dir)
                b.squares[capSq.idx] = null
            }
            b.squares[move.to.idx] = if (move.promotion != null) Piece(move.promotion, sideToMove) else moving
        }

        var wK = whiteCastleK; var wQ = whiteCastleQ; var bK = blackCastleK; var bQ = blackCastleQ
        fun touchesWhiteRookStart(from: Square) = (from.rank == 7 && (from.file == 0 || from.file == 7))
        fun touchesBlackRookStart(from: Square) = (from.rank == 0 && (from.file == 0 || from.file == 7))
        val movedPiece = pieceAt(move.from)
        if (movedPiece?.type == PieceType.KING && movedPiece.side == Side.WHITE) { wK = false; wQ = false }
        if (movedPiece?.type == PieceType.KING && movedPiece.side == Side.BLACK) { bK = false; bQ = false }
        if (movedPiece?.type == PieceType.ROOK && movedPiece.side == Side.WHITE && touchesWhiteRookStart(move.from)) {
            if (move.from.file == 0) wQ = false else wK = false
        }
        if (movedPiece?.type == PieceType.ROOK && movedPiece.side == Side.BLACK && touchesBlackRookStart(move.from)) {
            if (move.from.file == 0) bQ = false else bK = false
        }
        val cap = pieceAt(move.to)
        if (cap?.type == PieceType.ROOK && cap.side == Side.WHITE && move.to.rank == 7 && (move.to.file == 0 || move.to.file == 7)) {
            if (move.to.file == 0) wQ = false else wK = false
        }
        if (cap?.type == PieceType.ROOK && cap.side == Side.BLACK && move.to.rank == 0 && (move.to.file == 0 || move.to.file == 7)) {
            if (move.to.file == 0) bQ = false else bK = false
        }

        val newEp = when (movedPiece?.type) {
            PieceType.PAWN -> {
                val startRank = if (sideToMove == Side.WHITE) 6 else 1
                val twoRank   = if (sideToMove == Side.WHITE) 4 else 3
                if (move.from.rank == startRank && move.to.rank == twoRank) {
                    Square(move.from.file, if (sideToMove == Side.WHITE) 5 else 2)
                } else null
            }
            else -> null
        }

        return Board(b.squares, sideToMove.other(), wK, wQ, bK, bQ, newEp)
    }

    private fun copyMutable(): BoardMutable = BoardMutable(squares.copyOf())
    private class BoardMutable(val squares: Array<Piece?>)

    fun isSquareAttacked(target: Square, by: Side): Boolean {
        val knightD = arrayOf(1 to 2, 2 to 1, -1 to 2, -2 to 1, 1 to -2, 2 to -1, -1 to -2, -2 to -1)
        for ((df, dr) in knightD) {
            val f = target.file + df; val r = target.rank + dr
            if (f in 0..7 && r in 0..7) {
                val p = squares[r*8+f]
                if (p?.side == by && p.type == PieceType.KNIGHT) return true
            }
        }
        fun ray(dfs: Int, drs: Int, types: Set<PieceType>): Boolean {
            var f = target.file + dfs; var r = target.rank + drs
            while (f in 0..7 && r in 0..7) {
                val p = squares[r*8+f]
                if (p != null) { if (p.side == by && p.type in types) return true; break }
                f += dfs; r += drs
            }
            return false
        }
        if (ray(1,0,setOf(PieceType.ROOK,PieceType.QUEEN))) return true
        if (ray(-1,0,setOf(PieceType.ROOK,PieceType.QUEEN))) return true
        if (ray(0,1,setOf(PieceType.ROOK,PieceType.QUEEN))) return true
        if (ray(0,-1,setOf(PieceType.ROOK,PieceType.QUEEN))) return true
        if (ray(1,1,setOf(PieceType.BISHOP,PieceType.QUEEN))) return true
        if (ray(1,-1,setOf(PieceType.BISHOP,PieceType.QUEEN))) return true
        if (ray(-1,1,setOf(PieceType.BISHOP,PieceType.QUEEN))) return true
        if (ray(-1,-1,setOf(PieceType.BISHOP,PieceType.QUEEN))) return true

        val dir = if (by == Side.WHITE) -1 else 1
        for (df in intArrayOf(-1, 1)) {
            val f = target.file + df; val r = target.rank + dir
            if (f in 0..7 && r in 0..7) {
                val p = squares[r*8+f]
                if (p?.side == by && p.type == PieceType.PAWN) return true
            }
        }
        for (dr in -1..1) for (df in -1..1) if (dr!=0 || df!=0) {
            val f = target.file + df; val r = target.rank + dr
            if (f in 0..7 && r in 0..7) {
                val p = squares[r*8+f]
                if (p?.side == by && p.type == PieceType.KING) return true
            }
        }
        return false
    }

    fun kingSquare(side: Side): Square {
        for (i in 0 until 64) {
            val p = squares[i]
            if (p?.type == PieceType.KING && p.side == side) return Square.fromIdx(i)
        }
        error("King not found for $side")
    }

    companion object {
        fun start(): Board {
            val arr = Array<Piece?>(64) { null }
            fun put(rank: Int, file: Int, p: Piece) { arr[rank*8+file] = p }
            val back = arrayOf(PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK)
            for (f in 0..7) { put(0,f, Piece(back[f], Side.BLACK)); put(7,f, Piece(back[f], Side.WHITE)) }
            for (f in 0..7) { put(1,f, Piece(PieceType.PAWN, Side.BLACK)); put(6,f, Piece(PieceType.PAWN, Side.WHITE)) }
            return Board(arr, Side.WHITE, true, true, true, true, null)
        }
    }
}
