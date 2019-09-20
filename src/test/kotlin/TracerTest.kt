package cc.cheatex.ktracer

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

internal class TracerTest {
  val simpleScene = Scene(
      Camera(VectorD(10.0, 0.0, 0.0), VectorD(0.0, 0.0, 10.0), 1.57),
      listOf(Sphere(VectorD(10.0, 0.0, 0.0), 3.0, Material(ColorD.red, 1.0))),
      listOf(),
      ambientBrightness = 0.1,
      background = ColorD.green)

  @Test fun screen() {
    val tracer = Tracer(simpleScene, RenderingOptions(Resolution(1024, 768)))
    var hit = true
    for (y in 384..679) {
      val x = 512
      val direction = tracer.screen.getPixelCoordinates(x, y)
      val i = closestIntersection(simpleScene, Ray(VectorD.zero, direction))
      val nowHit = i is ObjectIntersection
      if (!hit and nowHit) {
        fail<Nothing>("Hit appeared again [$x, $y]")
      }
      hit = nowHit
    }
    hit = true
    for (x in 512..1024) {
      val y = 384
      val direction = tracer.screen.getPixelCoordinates(x, y)
      val i = closestIntersection(simpleScene, Ray(VectorD.zero, direction))
      val nowHit = i is ObjectIntersection
      if (!hit and nowHit) {
        fail<Nothing>("Hit appeared again [$x, $y]")
      }
      hit = nowHit
    }
  }

  @Test fun intersection() {
    val sphere = simpleScene.objects.first() as Sphere

    val i = intersection(Ray(VectorD.zero, sphere.pos), sphere)
    when (i) {
      is InfinityIntersection -> fail<Nothing>("Ray should intersect")
      is ObjectIntersection ->
        assertThat(i.hitNormal, equal(VectorD(-1.0, 0.0, 0.0)))
    }

    val i1 = intersection(Ray(VectorD.zero, sphere.pos + VectorD(0.0, 2.0, 2.0)), sphere)
    when (i1) {
      is InfinityIntersection -> fail<Nothing>("Ray should intersect")
      is ObjectIntersection ->
        assertThat(i1.hitNormal, closeTo(VectorD(-0.5, 0.5, 0.5), 0.2))
    }

    //touching in x-y plane
    val at = sphere.pos + VectorD(0.0, 3.14485451, 0.0)
    val i2 = intersection(Ray(VectorD.zero, at), sphere)
    when (i2) {
      is InfinityIntersection -> fail<Nothing>("Ray should intersect")
      is ObjectIntersection ->
        assertThat(i2.hitNormal, closeTo((at cross -VectorD.z).unit, E*10))
    }
  }
}
