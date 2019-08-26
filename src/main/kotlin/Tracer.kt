package cc.cheatex.ktracer

data class Resolution(val width: Int, val height: Int)

data class RenderingOptions(val resolution: Resolution,
                            val distance: Double,
                            val depth: Int,
                            val minWeight: Double,
                            val lightAttenuation: Boolean)

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

class Screen(camera: Camera, screenDistance: Double, resolution: Resolution)

class Tracer(val scene: Scene, val options: RenderingOptions) {
    val screenDistance = 1.0
    val screen = Screen(scene.camera, screenDistance, options.resolution)

    fun calcPixel(pixelPos: Pair<Int, Int>): ColorD =
        trace(Ray(VectorD.zero, VectorD.zero))

    fun trace(ray: Ray): ColorD =
            when(val i = closestIntersection(ray, scene)) {
                InfinityIntersection -> scene.background
                is ObjectIntersection -> shade(ray, i)
            }

    fun closestIntersection(ray: Ray, scene: Scene): Intersection = InfinityIntersection

    fun shade(ray: Ray, intersection: ObjectIntersection): ColorD = ColorD.white
}
