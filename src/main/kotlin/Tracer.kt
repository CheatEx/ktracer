package cc.cheatex.ktracer

import kotlin.math.tan

data class Resolution(val width: Int, val height: Int) {
  init {
    assert(width % 2 == 0)
    assert(height % 2 == 0)
  }
}

data class RenderingOptions(val resolution: Resolution)

sealed class Intersection

object InfinityIntersection : Intersection()

class ObjectIntersection(val hitPoint: VectorD,
                         val hitNormal: VectorD,
                         val obj: MaterialObject,
                         val t: Double)
  : Intersection()

private val finiteComparator: Comparator<ObjectIntersection> = compareBy { i -> i.t }

val intersectionComparator: Comparator<Intersection> = Comparator { o1, o2 ->
  when {
    o1 is ObjectIntersection && o2 is ObjectIntersection -> finiteComparator.compare(o1, o2)
    o1 is InfinityIntersection -> +1
    o2 is InfinityIntersection -> -1
    else -> 0
  }
}

open class Ray(val origin: VectorD, val direction: VectorD)

class ExtendedRay(origin: VectorD,
                  direction: VectorD,
                  val obj: SceneObject)
  : Ray(origin, direction)

class Screen(camera: Camera, val resolution: Resolution) {
  val density: Double = tan(camera.viewport/2)*2/resolution.width

  val nx: VectorD = (camera.direction cross camera.up) * density
  val ny: VectorD = (camera.direction cross (camera.direction cross camera.up)) * density

  val screenCenter: VectorD = camera.direction / camera.direction.length

  fun getPixelCoordinates(x: Int, y: Int): VectorD =
      screenCenter +
          (nx * (x - resolution.width/2).toDouble()) +
          (ny * (y - resolution.height/2).toDouble())
}

private const val E = 1e-6

class Tracer(val scene: Scene, val options: RenderingOptions) {
  val screen = Screen(scene.camera, options.resolution)

  fun render(): Image {
    val image: Image =
        Array(options.resolution.height) {
          Array(options.resolution.width) {ColorD.white}
        }

    for (y in 0.until(options.resolution.height)) {
      for (x in 0.until(options.resolution.width))
        image[y][x] = calcPixel(x, y)
    }

    return image
  }

  fun calcPixel(x: Int, y: Int): ColorD {
    val pixelCoordinates = screen.getPixelCoordinates(x, y)
//    if ((x==100 || x==200 || x==300 || x==500 || x==600) && (y==0 || y==350 || y==700))
//      println("Pixel[$x, $y] =$pixelCoordinates")
    val trace = trace(Ray(VectorD.zero, pixelCoordinates - VectorD.zero))
    return trace
  }

  fun trace(ray: Ray): ColorD =
      when(val i = closestIntersection(ray)) {
        InfinityIntersection -> scene.background
        is ObjectIntersection -> shade(ray, i)
      }

  fun closestIntersection(ray: Ray): Intersection =
      scene.objects
          .map { intersection(ray, it) }
          .minWith(intersectionComparator)
          ?: InfinityIntersection

  fun intersection(ray: Ray, obj: MaterialObject): Intersection {
    return when (obj) {
      is Sphere -> intersectSphere(ray, obj)
    }
  }

  private fun intersectSphere(ray: Ray, obj: Sphere): Intersection {
    val t = ray.origin - obj.pos
    val A = ray.direction.sumsq
    val B = ray.direction.dot(t) * 2
    val C = t.sumsq - (obj.radius * obj.radius)
    var temp = B * B - 4 * A * C

    var t0 = 0.0
    var t1 = 0.0
    var t2 = 0.0
    if (temp < 0) {
      return InfinityIntersection
    } else if (temp > 0) {
      temp = Math.sqrt(temp)
      t1 = (-B + temp) / (2 * A)
      t2 = (-B - temp) / (2 * A)
      if (t1 > t2) {
        t0 = t1
        t1 = t2
        t2 = t0
      }

      t0 = 0.0
      if (t1 > E)
        t0 = t1
      if (t2 > 0 && t0 <= E)
        t0 = t2
      if (t0 < E)
        return InfinityIntersection
    } else {
      t0 = -B / (2 * A)
      if (t0 < E)
        return InfinityIntersection
    }
    val p = (ray.direction * t0) + ray.origin
    val n = (p - obj.pos) / obj.radius

    return ObjectIntersection(p, n, obj, t0)
  }

  fun shade(ray: Ray, intersection: ObjectIntersection): ColorD {
    val color = ambientColor(intersection)

    for (light in scene.lights) {
      //there is direction to light from hit point
      val lightDirection = light.pos - intersection.hitPoint
      val lightDistance = lightDirection.length
      val localIntensity = light.color.copy()

//      if (light is SpotLight) {
//        //this is direction of spot
//        val spotDirection = light.direction
//        val hitPointDirection = intersection.hitPoint - light.pos
//
//        val spotAngle = light.spread
//        val hitPointAngle = acos(spotDirection.dot(hitPointDirection))
//        if (hitPointAngle > spotAngle || hitPointAngle < 0) {
//          continue
//        }
//        val falloff = 1 - (hitPointAngle / spotAngle).pow(2)
//        localIntensity *= falloff
//      }

    }

//    color *= intersection.obj.material.reflection
//        color += trace(computeReflectedRay)*intersection.obj.material.rC
//        color += trace(computeTransmittedRay)*intersection.obj.material.tC

    return color
  }

  fun reflection(light: ColorD, material: ColorD, k: Double, shadowAttenuation: Double): ColorD {
    val color: ColorD = light.copy()
    color *= material
    color *= k * shadowAttenuation
    return color
  }

  private fun computeShadowAttenuation(shadowRay: Ray, lightDistance: Double): Double {
    return 0.0
  }

  fun ambientColor(intersection: ObjectIntersection): ColorD =
      scene.ambientLight.multiply(intersection.obj.material.color)
}
