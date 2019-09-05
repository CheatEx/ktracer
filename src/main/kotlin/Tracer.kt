package cc.cheatex.ktracer

import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.tan

data class Resolution(val width: Int, val height: Int)

data class RenderingOptions(val resolution: Resolution,
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

class Screen(val camera: Camera, val screenDistance: Double, resolution: Resolution) {
    val width: Double = tan(camera.viewport/2)*screenDistance*2
    val density: Double = width/resolution.width
    val height: Double = resolution.height*density

    // TODO find screen coordinate system
    val nx: VectorD = VectorD.zero
    val ny: VectorD = VectorD.zero

    val screenCenter: VectorD
        get() = camera.at * screenDistance / camera.at.length

    val topBottom: VectorD
        get() = screenCenter + screen2Absolute(-width/2, -height/2)

    fun getPixelCoordinates(x: Int, y: Int): VectorD =
        topBottom + screen2Absolute(density*x, density*y)

    fun screen2Absolute(x: Double, y: Double) = nx*x + ny*y
}

private const val E = 1e-6

class Tracer(val scene: Scene, val options: RenderingOptions) {
    val screenDistance = 1.0
    val screen = Screen(scene.camera, screenDistance, options.resolution)

    fun render(): Image {
        val image: Image =
                Array(options.resolution.width) {
                    Array(options.resolution.width) {ColorD.white}
                }

        for (i in 0..options.resolution.width) {
            for (j in 0..options.resolution.height)
                image[i][j] = calcPixel(i, j)
        }

        return image
    }

    fun trace(ray: Ray): ColorD =
            when(val i = closestIntersection(ray, scene)) {
                InfinityIntersection -> scene.background
                is ObjectIntersection -> shade(ray, i)
            }

    fun calcPixel(x: Int, y: Int): ColorD =
        trace(Ray(VectorD.zero, screen.getPixelCoordinates(x, y) - VectorD.zero))

    fun closestIntersection(ray: Ray, scene: Scene): Intersection =
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

            if (light is SpotLight) {
                //this is direction of spot
                val spotDirection = -light.at
                val hitPointDirection = (light.pos - intersection.hitPoint).unit

                val spotAngle = light.spread
                val hitPointAngle = acos(spotDirection.dot(hitPointDirection))
                if (hitPointAngle > spotAngle || hitPointAngle < 0) {
                    continue
                }
                val falloff = 1 - (hitPointAngle / spotAngle).pow(2)
                localIntensity *= falloff
            }

            if (options.lightAttenuation) {
                val falloff = 1.0 - lightDistance.pow(2)
                localIntensity *= falloff
            }

            val viewingDirection = -ray.direction
            val neh = lightDirection + viewingDirection

            val nl = intersection.hitNormal.dot(lightDirection)

            if (nl > 0) {
                val shadowRayDirection = light.pos - intersection.hitPoint.unit
                val shadowRayOrigin = shadowRayDirection * 1e-3 + intersection.hitPoint
                val shadowRay = Ray(shadowRayOrigin, shadowRayDirection)
                val shadowAttenuation = computeShadowAttenuation(shadowRay, lightDistance)

                color += lightReflection(localIntensity, intersection.obj.material.Kd, nl, shadowAttenuation)

                val nh = intersection.hitNormal.dot(neh).pow(intersection.obj.material.alpha)
                color += lightReflection(localIntensity, intersection.obj.material.Ks, nh, shadowAttenuation)
            }
        }

        color *= intersection.obj.material.lC
//        color += trace(computeReflectedRay)*intersection.obj.material.rC
//        color += trace(computeTransmittedRay)*intersection.obj.material.tC

        return color
    }

    fun lightReflection(intensity: ColorD, k: ColorD, n: Double, shadowAttenuation: Double): ColorD {
        val color: ColorD = intensity.copy()
        color *= k
        color *= n * shadowAttenuation
        return color
    }

    private fun computeShadowAttenuation(shadowRay: Ray, lightDistance: Double): Double {
        return 0.0
    }

    fun ambientColor(intersection: ObjectIntersection): ColorD =
        scene.ambientLight.multiply(intersection.obj.material.Ka)
}
