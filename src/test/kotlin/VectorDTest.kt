package cc.cheatex.ktracer

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class VectorDTest {
  val v123 = VectorD(1.0, 2.0, 3.0)
  val x = VectorD(1.0, 0.0, 0.0)
  val y = VectorD(0.0, 1.0, 0.0)
  val z = VectorD(0.0, 0.0, 1.0)

  @Test fun constructor() {
    VectorD(1.0, 2.0, 3.0)
    VectorD.zero
  }

  @Test fun add() {
    assertThat(v123 + VectorD.zero, equal(v123))
  }

  @Test fun cross() {
    assertThat(x cross y, equal(z))
    assertThat(x cross z, equal(-y))
    assertThat(x cross (x cross z), equal(-z))
  }
}
