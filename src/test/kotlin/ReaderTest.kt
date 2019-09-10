package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

const val TEST_SCENE_FILE = "/test.ktrc"

class ReaderTest {
  @Test fun read() {
    val input = javaClass.getResourceAsStream(TEST_SCENE_FILE)
    val parseResult = SceneParser.tryParseToEnd(input)
    when (parseResult) {
      is Parsed<Scene> -> {
        val scene = parseResult.value
        assertThat(scene.background, equal(ColorD(0.5, 0.5, 0.5)))
        assertThat(scene.background, equal(ColorD(0.5, 0.5, 0.5)))
        assertEquals(0.1, scene.ambientBrightness)

        assertThat(scene.camera.at, equal(VectorD(10.0, 0.0, 0.0)))
        assertThat(scene.camera.upo, equal(VectorD(0.0, 0.0, 10.0)))
        assertEquals(1.57, scene.camera.viewport)
      }
      is ErrorResult -> fail(parseResult.toString())
    }
  }
}
