package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

const val TEST_SCENE_FILE = "/test.ktrc"

class ReaderTest {
  @Test fun read() {
    val input = javaClass.getResourceAsStream(TEST_SCENE_FILE)
    val parseResult = SceneParser.tryParseToEnd(input)
    assert(parseResult is Parsed<Scene>)
    when (parseResult) {
      is Parsed<Scene> -> {
        val scene = parseResult.value
        assertArrayEquals(ColorD(0.5, 0.5, 0.5).array, scene.background.array)
        assertArrayEquals(ColorD(0.05, 0.05, 0.05).array, scene.ambientLight.array)
      }
      is ErrorResult -> fail(parseResult.toString())
    }
  }
}
