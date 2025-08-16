package com.timur.chessiq.ui.glossy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GlossyBoardDemo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "ChessIQ – Parlak Tahta",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(8.dp)
        )

        // Kare kare satranç tahtası
        Column(modifier = Modifier.size(320.dp)) {
            repeat(8) { row ->
                Row(modifier = Modifier.weight(1f)) {
                    repeat(8) { col ->
                        val isLight = (row + col) % 2 == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            GlossySquareCell(isLight)

                            // Başlangıç taşları (Unicode)
                            val pieceChar: Char? = when (row) {
                                0 -> "♜♞♝♛♚♝♞♜"[col]  // Siyah arka sıra
                                1 -> '♟'                  // Siyah piyon
                                6 -> '♙'                  // Beyaz piyon
                                7 -> "♖♘♗♕♔♗♘♖"[col]  // Beyaz arka sıra
                                else -> null
                            }
                            if (pieceChar != null) {
                                GlossyUnicodePiece(
                                    symbol = pieceChar,
                                    isWhite = row >= 6
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlossySquareCell(isLight: Boolean) {
    val base = if (isLight) Color(0xFFF1E8C7) else Color(0xFF8AA05A)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // taban kare
        drawRect(
            brush = Brush.linearGradient(
                listOf(base.copy(alpha = 0.95f), base, base.copy(alpha = 0.9f)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            size = size
        )
        // üst parlaklık
        val glossHeight = size.height * 0.4f
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.2f), Color.Transparent),
                startY = 0f,
                endY = glossHeight
            ),
            size = androidx.compose.ui.geometry.Size(size.width, glossHeight)
        )
    }
}

@Composable
private fun GlossyUnicodePiece(symbol: Char, isWhite: Boolean) {
    Text(
        text = symbol.toString(),
        fontSize = 24.sp,
        color = if (isWhite) Color(0xFFFAFAFA) else Color(0xFF111111),
        fontWeight = FontWeight.Bold
    )
}
