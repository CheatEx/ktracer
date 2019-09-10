package cc.cheatex.ktracer

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class VectorDTest {
  val v123 = VectorD(1.0, 2.0, 3.0)

  @Test fun constructor() {
    VectorD(1.0, 2.0, 3.0)
    VectorD.zero
  }

  @Test fun add() {
    assertThat(v123 + VectorD.zero, equal(v123))
  }

  @Test fun cross() {
    assertThat(VectorD.x cross VectorD.y, equal(VectorD.z))
    assertThat(VectorD.x cross VectorD.z, equal(-VectorD.y))
    assertThat(VectorD.x cross (VectorD.x cross VectorD.z), equal(-VectorD.z))
  }
}
