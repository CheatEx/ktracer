package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

object NotImplementedResult: ErrorResult()

object NotImplemented: Parser<Nothing> {
  override fun tryParse(tokens: Sequence<TokenMatch>): ParseResult<Nothing> = NotImplementedResult
}

object SceneParser : Grammar<Scene>() {
  val whiteSpace  by token("\\s+", ignore = true)
  val floatNum    by token("(-?\\d+(\\.\\d*)?|\\d*\\.\\d+)")
  val open        by token("\\{")
  val close       by token("\\}")
  val eq          by token("=")

  val scene         by token("scene")
  val background    by token("background")
  val ambientLight  by token("ambientLight")

  val FloatNum: Parser<Double> = floatNum map { nt ->
    nt.text.toDouble()
  }
  val Color: Parser<ColorD> = FloatNum and FloatNum and FloatNum map { (r, g, b) ->
    ColorD(r, g, b) }

  val AmbientLight: Parser<ColorD> = skip(ambientLight) and skip(eq) and Color
  val Background: Parser<ColorD> = skip(background) and skip(eq) and Color
  val SceneBody: Parser<Scene> = Background and AmbientLight map { (b, al) ->
    Scene(Camera(VectorD.zero, VectorD.zero, 0.0), emptyList(), emptyList(), al, b) }
  val Scene: Parser<Scene> = skip(scene) and skip(open) and SceneBody and skip(close)

  override val rootParser: Parser<Scene> = Scene
}

fun parseScene(input: InputStream): Scene {
  val reader = BufferedReader(InputStreamReader(input))

  val background = readColor(reader)
  val ambientLight = readColor(reader)
  val camera = readCamera(reader)
  val lights = readLights(reader)
  val objects = readObjects(reader)

  return Scene(camera, objects, lights, background, ambientLight)
}

private fun readCamera(reader: BufferedReader): Camera {
  val name = reader.readLine().trim()
  val at = readVector(reader)
  val up = readVector(reader)
  val viewport = reader.readLine().trim().toDouble()

  return Camera(at, up, viewport)
}

private fun readLights(reader: BufferedReader): List<Light> =
    readObjectGroup(reader, ::readPointLight) +
        readObjectGroup(reader, ::readSpotLight)

private fun readObjects(reader: BufferedReader): List<MaterialObject> =
    readObjectGroup(reader, ::readSphere)

private fun readPointLight(reader: BufferedReader): PointLight {
  val name = reader.readLine().trim()
  val position = readVector(reader)
  val intensity = readColor(reader)

  return PointLight(position, intensity)
}

private fun readSpotLight(reader: BufferedReader): SpotLight {
  val name = reader.readLine().trim()
  val position = readVector(reader)
  val at = readVector(reader)
  val angle = reader.readLine().trim().toDouble()
  val intensity = readColor(reader)

  return SpotLight(position, at, angle, intensity)
}

private fun <T, R : T> readObjectGroup(reader: BufferedReader,
                                       objectReader: (BufferedReader) -> R): List<T> {
  reader.readLine()
  val objectsCount = reader.readLine().trim().toInt()

  return sequence {
    for (i in 0..objectsCount) yield(objectReader(reader))
  } .toList()
}

private fun readSphere(reader: BufferedReader): Sphere {
  val name = reader.readLine().trim()
  val position = readVector(reader)
  val radius = reader.readLine().trim().toDouble()
  val material = readMaterial(reader)

  return Sphere(position, material, radius)
}

private fun readMaterial(reader: BufferedReader): Material {
  return Material(ColorD.white, ColorD.white, ColorD.white, 1.0, 1.0, 1.0, 1.0, 1.0)
}

private fun readVector(reader: BufferedReader): VectorD {
  val tokenizer = StringTokenizer(reader.readLine())
  return VectorD(tokenizer.nextToken().toDouble(),
      tokenizer.nextToken().toDouble(),
      tokenizer.nextToken().toDouble())
}

private fun readColor(reader: BufferedReader): ColorD {
  val tokenizer = StringTokenizer(reader.readLine())
  return ColorD(tokenizer.nextToken().toDouble(),
      tokenizer.nextToken().toDouble(),
      tokenizer.nextToken().toDouble())
}
