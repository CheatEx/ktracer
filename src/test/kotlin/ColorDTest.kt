package cc.cheatex.ktracer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ColorDTest {
  @Test fun rgb() {
    assertEquals(0x00FFFFFF  ,ColorD.white.rgbInt())
    assertEquals(0x00FF0000 ,ColorD.red.rgbInt())
    assertEquals(0x0000FF00 ,ColorD.green.rgbInt())
    assertEquals(0x000000FF ,ColorD.blue.rgbInt())
    assertEquals(0x00000000 ,ColorD.black.rgbInt())
  }
}