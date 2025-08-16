package com.timur.chessiq.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timur.chessiq.engine.*

@Composable
fun ChessScreen() {
    var board by remember { mutableStateOf(Board.start()) }
    var selected by remember { mutableStateOf<Square?>(null) }
    var legal by remember { mutableStateOf(emptyList<Move>()) }
    var status by remember { mutableStateOf("Beyaz başlar") }
    var promoFor by remember { mutableStateOf<Move?>(null) }

    fun refreshStatus(b: Board) {
        val moves = MoveGen.legalMoves(b)
        status = when {
            moves.isEmpty() && b.isSquareAttacked(b.kingSquare(b.sideToMove), b.sideToMove.other())
            -> "ŞAH MAT! ${b.sideToMove.other()} kazandı"
            moves.isEmpty() -> "PAT!"
            else -> "${if (b.sideToMove==Side.WHITE) "Beyaz" else "Siyah"} düşünüyor"
        }
    }
    LaunchedEffect(Unit) { refreshStatus(board) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Text("ChessIQ", fontSize = 20.sp, modifier = Modifier.padding(8.dp))

        Column(modifier = Modifier.size(360.dp)) {
            repeat(8) { r ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(8) { f ->
                        val sq = Square(f, r)
                        val isLight = (f + r) % 2 == 0
                        val on = board.pieceAt(sq)
                        val isSel = selected?.idx == sq.idx
                        val isTarget = legal.any { it.to.idx == sq.idx }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isLight) Color(0xFFF1E8C7) else Color(0xFF8AA05A))
                                .then(if (isSel) Modifier.border(3.dp, Color(0xFF7CB342)) else Modifier)
                                .then(if (isTarget) Modifier.border(3.dp, Color(0xFF64B5F6)) else Modifier)
                                .clickable {
                                    val myPiece = on?.side == board.sideToMove
                                    if (selected == null) {
                                        if (myPiece) {
                                            selected = sq
                                            legal = MoveGen.legalMoves(board).filter { it.from.idx == sq.idx }
                                        }
                                    } else {
                                        val mv = legal.firstOrNull { it.to.idx == sq.idx }
                                        if (mv != null) {
                                            if (mv.promotion != null) {
                                                promoFor = mv
                                            } else {
                                                board = board.make(mv); selected = null; legal = emptyList()
                                                refreshStatus(board)
                                            }
                                        } else {
                                            if (myPiece) {
                                                selected = sq
                                                legal = MoveGen.legalMoves(board).filter { it.from.idx == sq.idx }
                                            } else {
                                                selected = null; legal = emptyList()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) { if (on != null) Text(on.unicode(), fontSize = 26.sp) }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row {
            Button(onClick = { board = Board.start(); selected = null; legal = emptyList(); refreshStatus(board) }) {
                Text("Yeni Oyun")
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(status)

        val pending = promoFor
        if (pending != null) {
            PromotionDialog(side = board.sideToMove, onPick = { type ->
                board = board.make(pending.copy(promotion = type))
                promoFor = null; selected = null; legal = emptyList()
                refreshStatus(board)
            }, onCancel = { promoFor = null; selected = null; legal = emptyList() })
        }
    }
}

@Composable
private fun PromotionDialog(side: Side, onPick: (PieceType)->Unit, onCancel: ()->Unit) {
    val opts = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Terfi") },
        text = { Text("Yeni taş seçin") },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                opts.forEach {
                    TextButton(onClick = { onPick(it) }) { Text(Piece(it, side).unicode(), fontSize = 24.sp) }
                }
            }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Vazgeç") } }
    )
}
