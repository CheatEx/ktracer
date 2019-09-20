package cc.cheatex.ktracer

const val Eps = 1e-6

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

open class Ray(val origin: VectorD, val at: VectorD) {
  val direction: VectorD = at.unit
}

class ExtendedRay(origin: VectorD,
                  direction: VectorD,
                  val obj: SceneObject)
  : Ray(origin, direction)

fun closestIntersection(scene: Scene, ray: Ray): Intersection =
    scene.objects
        .map { intersection(ray, it) }
        .minWith(intersectionComparator)
        ?: InfinityIntersection

interface Body<F> {
  fun intersection(ray: Ray, obj: F): Intersection
}

object SphereBody: Body<Sphere> {
  override fun intersection(ray: Ray, obj: Sphere): Intersection {
    val t = ray.origin - obj.pos
    val A = ray.at.sumsq
    val B = ray.at.dot(t) * 2
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
      if (t1 > Eps)
        t0 = t1
      if (t2 > 0 && t0 <= Eps)
        t0 = t2
      if (t0 < Eps)
        return InfinityIntersection
    } else {
      t0 = -B / (2 * A)
      if (t0 < Eps)
        return InfinityIntersection
    }
    val p = (ray.at * t0) + ray.origin
    val n = (p - obj.pos) / obj.radius

    return ObjectIntersection(p, n, obj, t0)
  }
}

fun intersection(ray: Ray, obj: MaterialObject): Intersection {
  return when (obj) {
    is Sphere -> SphereBody.intersection(ray, obj)
  }
}
