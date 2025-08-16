package com.timur.chessiq.engine

object MoveGen {

    /** Yasal (şahı açıkta bırakmayan) hamleler */
    fun legalMoves(board: Board): List<Move> {
        val side = board.sideToMove
        val opp  = side.other()
        return pseudoLegal(board).filter { mv ->
            val next = board.make(mv)
            // Hamleyi yapan tarafın (side) kralı, yeni konumda rakip (opp) tarafından saldırı altında olmamalı
            !next.isSquareAttacked(next.kingSquare(side), opp)
        }
    }

    /** Şah kontrolü yapmadan tüm olası (pseudo-legal) hamleler */
    private fun pseudoLegal(b: Board): List<Move> {
        val side = b.sideToMove
        val moves = mutableListOf<Move>()

        fun add(m: Move) { moves.add(m) }

        for (i in 0 until 64) {
            val from = Square.fromIdx(i)
            val p = b.pieceAt(from) ?: continue
            if (p.side != side) continue

            when (p.type) {
                PieceType.PAWN   -> genPawn(b, from, side, ::add)
                PieceType.KNIGHT -> genKnight(b, from, side, ::add)
                PieceType.BISHOP -> genSlide(b, from, side, ::add,
                    arrayOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
                PieceType.ROOK   -> genSlide(b, from, side, ::add,
                    arrayOf(1 to 0, -1 to 0, 0 to 1, 0 to -1))
                PieceType.QUEEN  -> genSlide(b, from, side, ::add,
                    arrayOf(1 to 1, 1 to -1, -1 to 1, -1 to -1, 1 to 0, -1 to 0, 0 to 1, 0 to -1))
                PieceType.KING   -> genKing(b, from, side, ::add)
            }
        }
        return moves
    }

    private fun genPawn(b: Board, from: Square, side: Side, add: (Move) -> Unit) {
        val dir = if (side == Side.WHITE) -1 else 1
        val startRank = if (side == Side.WHITE) 6 else 1
        val lastRank  = if (side == Side.WHITE) 0 else 7

        fun push(to: Square) {
            val promo = if (to.rank == lastRank) PieceType.QUEEN else null
            add(Move(from, to, promotion = promo))
        }

        // 1 ileri
        val one = Square(from.file, from.rank + dir)
        if (one.rank in 0..7 && b.pieceAt(one) == null) {
            push(one)
            // 2 ileri (başlangıçtan)
            if (from.rank == startRank) {
                val two = Square(from.file, from.rank + 2 * dir)
                if (b.pieceAt(two) == null) add(Move(from, two))
            }
        }
        // Çapraz alma
        for (df in intArrayOf(-1, 1)) {
            val f = from.file + df
            val r = from.rank + dir
            if (f in 0..7 && r in 0..7) {
                val to = Square(f, r)
                val t = b.pieceAt(to)
                if (t != null && t.side != side) {
                    val promo = if (to.rank == lastRank) PieceType.QUEEN else null
                    add(Move(from, to, promotion = promo, isCapture = true))
                }
            }
        }
        // En passant
        val ep = b.enPassant
        if (ep != null && ep.rank == from.rank + dir && kotlin.math.abs(ep.file - from.file) == 1) {
            add(Move(from, ep, isEnPassant = true))
        }
    }

    private fun genKnight(b: Board, from: Square, side: Side, add: (Move) -> Unit) {
        val d = arrayOf(1 to 2, 2 to 1, -1 to 2, -2 to 1, 1 to -2, 2 to -1, -1 to -2, -2 to -1)
        for ((df, dr) in d) {
            val f = from.file + df; val r = from.rank + dr
            if (f in 0..7 && r in 0..7) {
                val to = Square(f, r); val t = b.pieceAt(to)
                if (t == null || t.side != side) add(Move(from, to, isCapture = t != null))
            }
        }
    }

    private fun genSlide(
        b: Board, from: Square, side: Side, add: (Move) -> Unit, dirs: Array<Pair<Int, Int>>
    ) {
        for ((df, dr) in dirs) {
            var f = from.file + df; var r = from.rank + dr
            while (f in 0..7 && r in 0..7) {
                val to = Square(f, r); val t = b.pieceAt(to)
                if (t == null) add(Move(from, to))
                else { if (t.side != side) add(Move(from, to, isCapture = true)); break }
                f += df; r += dr
            }
        }
    }

    private fun genKing(b: Board, from: Square, side: Side, add: (Move) -> Unit) {
        for (dr in -1..1) for (df in -1..1) if (df != 0 || dr != 0) {
            val f = from.file + df; val r = from.rank + dr
            if (f in 0..7 && r in 0..7) {
                val to = Square(f, r); val t = b.pieceAt(to)
                if (t == null || t.side != side) add(Move(from, to, isCapture = t != null))
            }
        }
        // Rok
        val rank = if (side == Side.WHITE) 7 else 0
        val enemy = side.other()
        // Kısa rok
        val canK = (side == Side.WHITE && b.whiteCastleK) || (side == Side.BLACK && b.blackCastleK)
        if (canK &&
            b.pieceAt(Square(5, rank)) == null && b.pieceAt(Square(6, rank)) == null &&
            !b.isSquareAttacked(Square(4, rank), enemy) &&
            !b.isSquareAttacked(Square(5, rank), enemy) &&
            !b.isSquareAttacked(Square(6, rank), enemy)
        ) add(Move(from, Square(6, rank), isCastleKing = true))
        // Uzun rok
        val canQ = (side == Side.WHITE && b.whiteCastleQ) || (side == Side.BLACK && b.blackCastleQ)
        if (canQ &&
            b.pieceAt(Square(1, rank)) == null && b.pieceAt(Square(2, rank)) == null && b.pieceAt(Square(3, rank)) == null &&
            !b.isSquareAttacked(Square(4, rank), enemy) &&
            !b.isSquareAttacked(Square(3, rank), enemy) &&
            !b.isSquareAttacked(Square(2, rank), enemy)
        ) add(Move(from, Square(2, rank), isCastleQueen = true))
    }
}
