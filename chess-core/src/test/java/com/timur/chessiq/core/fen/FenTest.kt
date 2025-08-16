package com.timur.chessiq.core.fen

import com.timur.chessiq.core.model.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class FenTest {
    @Test
    fun start_position_roundtrip() {
        val gs = fenToState(START_FEN)
        assertEquals(Color.WHITE, gs.sideToMove)
        val back = stateToFen(gs)
        // halfmove/fullmove normalize olabilir; bu yüzden baştaki 4 alanı karşılaştıralım
        assertEquals(START_FEN.split(" ").take(4), back.split(" ").take(4))
    }
}
