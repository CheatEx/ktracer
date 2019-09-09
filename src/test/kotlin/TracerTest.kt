package cc.cheatex.ktracer

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class TracerTest {
  val simpleScene = Scene(
      Camera(VectorD(10.0, 0.0, 0.0), VectorD(0.0, 0.0, 10.0), 1.57),
      listOf(Sphere(VectorD(10.0, 0.0, 0.0), 3.0, Material(ColorD.red, 1.0))),
      listOf(),
      ambientLight = ColorD.white,
      background = ColorD.green)

  @Test fun intersection() {
    val tracer = Tracer(simpleScene, RenderingOptions(Resolution(1024, 768), false))
    var hit = true
    for (y in 384..679) {
      val x = 512
      val direction = tracer.screen.getPixelCoordinates(x, y)
      val i = tracer.closestIntersection(Ray(VectorD.zero, direction))
      val nowHit = i is ObjectIntersection
      if (!hit and nowHit) {
        fail<Nothing>("Hit appeared again $x, $y]")
      }
      hit = nowHit
    }
    hit = true
    for (x in 512..1024) {
      val y = 384
      val direction = tracer.screen.getPixelCoordinates(x, y)
      val i = tracer.closestIntersection(Ray(VectorD.zero, direction))
      val nowHit = i is ObjectIntersection
      if (!hit and nowHit) {
        fail<Nothing>("Hit appeared again $x, $y]")
      }
      hit = nowHit
    }
  }
}
