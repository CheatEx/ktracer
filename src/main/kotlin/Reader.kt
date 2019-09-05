package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.Token
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
  val camera        by token("camera")
  val direction     by token("direction")
  val up            by token("up")
  val viewport      by token("viewport")
  val pointLight    by token("pointLight")
  val spotLight     by token("spotLight")
  val position      by token("position")
  val color         by token("color")
  val spread        by token("spread")

  val FloatNum: Parser<Double> = floatNum map { nt ->
    nt.text.toDouble()
  }
  val Color: Parser<ColorD> = FloatNum and FloatNum and FloatNum map { (r, g, b) ->
    ColorD(r, g, b) }
  val Vector: Parser<VectorD> = FloatNum and FloatNum and FloatNum map { (x, y, z) ->
    VectorD(x, y, z) }

  inline fun <reified T> value(key: Token, value: Parser<T>): Parser<T> =
      skip(key) and skip(eq) and value

  val AmbientLight: Parser<ColorD> = value(ambientLight, Color)
  val Background: Parser<ColorD> = value(background, Color)
  val Camera: Parser<Camera> =
      skip(camera) and skip(open) and
      value(direction, Vector) and
      value(up, Vector) and
      value(viewport, FloatNum) and
      skip(close) map { (d, u, v) ->
        Camera(d, u, v) }
  val PointLight: Parser<PointLight> =
      skip(pointLight) and skip(open) and
      value(position, Vector) and
      value(color, Color) and
      skip(close) map { (p, c) ->
        PointLight(p, c) }
  val SpotLight: Parser<SpotLight> =
      skip(spotLight) and skip(open) and
      value(position, Vector) and
      value(direction, Vector) and
      value(spread, FloatNum) and
      value(color, Color) and
      skip(close) map { (p, d, a, c) ->
        SpotLight(p, d, a, c) }
  val Light: Parser<Light> = PointLight or SpotLight
  val SceneBody: Parser<Scene> =
      Background and
      AmbientLight and
      Camera and
      zeroOrMore(Light) map { (b, al, c) ->
        Scene(c, emptyList(), emptyList(), al, b) }

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
