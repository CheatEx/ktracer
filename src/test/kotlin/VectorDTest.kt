package cc.cheatex.ktracer

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

internal class VectorDTest {
    val v123 = VectorD(1.0, 2.0, 3.0)

    @Test fun constructor() {
        VectorD(1.0, 2.0, 3.0)
        VectorD.zero
    }

    @Test fun add() {
        assertArrayEquals(v123.array, (v123 + VectorD.zero).array)
    }
}