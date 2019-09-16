package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parser

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
  val ambientBrightness  by token("ambientBrightness")
  val camera        by token("camera")
  val at            by token("at")
  val up            by token("up")
  val viewport      by token("viewport")
  val pointLight    by token("pointLight")
  val spotLight     by token("spotLight")
  val directedLight by token("directedLight")
  val position      by token("position")
  val color         by token("color")
  val brightness    by token("brightness")
  val spread        by token("spread")
  val material      by token("material")
  val diffuse       by token("diffuse")
  val sphere        by token("sphere")
  val radius        by token("radius")

  val FloatNum: Parser<Double> = floatNum map { nt ->
    nt.text.toDouble()
  }
  val Color: Parser<ColorD> = FloatNum and FloatNum and FloatNum map { (r, g, b) ->
    ColorD(r, g, b) }
  val Vector: Parser<VectorD> = FloatNum and FloatNum and FloatNum map { (x, y, z) ->
    VectorD(x, y, z) }

  inline fun <reified T> value(key: Token, value: Parser<T>): Parser<T> =
      skip(key) and skip(eq) and value

  val AmbientLight: Parser<Double> = value(ambientBrightness, FloatNum)
  val Background: Parser<ColorD> = value(background, Color)
  val Camera: Parser<Camera> =
      skip(camera) and skip(open) and
      value(at, Vector) and
      value(up, Vector) and
      value(viewport, FloatNum) and
      skip(close) map { (at, u, v) ->
        Camera(at, u, v) }
  val PointLight: Parser<PointLight> =
      skip(pointLight) and skip(open) and
      value(position, Vector) and
      value(brightness, FloatNum) and
      skip(close) map { (p, b) ->
        PointLight(p, b) }
  val SpotLight: Parser<SpotLight> =
      skip(spotLight) and skip(open) and
      value(position, Vector) and
      value(at, Vector) and
      value(brightness, FloatNum) and
      value(spread, FloatNum) and
      skip(close) map { (p, at, b, s) ->
        SpotLight(p, at, s, b) }
  val DirectedLight: Parser<DirectedLight> =
      skip(directedLight) and skip(open) and
      value(at, Vector) and
      value(brightness, FloatNum) and
      skip(close) map { (at, b) ->
        DirectedLight(at, b) }
  val Material: Parser<Material> =
      skip(material) and skip(open) and
      value(color, Color) and
      value(diffuse, FloatNum) and
      skip(close) map { (c, dr) ->
        Material(c, dr) }
  val Sphere: Parser<Sphere> =
      skip(sphere) and skip(open) and
      value(position, Vector) and
      value(radius, FloatNum) and
      Material and
      skip(close) map { (p, r, m) ->
        Sphere(p, r, m) }
  val Object: Parser<MaterialObject> = Sphere
  val Light: Parser<Light> = PointLight or SpotLight or DirectedLight
  val SceneBody: Parser<Scene> =
      Background and
      AmbientLight and
      Camera and
      zeroOrMore(Light) and
      zeroOrMore(Object) map { (b, al, c, ls, os) ->
        Scene(c, os, ls, al, b) }

  val Scene: Parser<Scene> = skip(scene) and skip(open) and SceneBody and skip(close)

  override val rootParser: Parser<Scene> = Scene
}
