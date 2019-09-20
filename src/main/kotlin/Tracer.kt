package cc.cheatex.ktracer

import kotlin.math.*

data class Resolution(val width: Int, val height: Int) {
  init {
    assert(width % 2 == 0)
    assert(height % 2 == 0)
  }
}

data class RenderingOptions(val resolution: Resolution)



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
    return trace(Ray(VectorD.zero, pixelCoordinates - VectorD.zero))
  }

  fun trace(ray: Ray): ColorD =
      when(val i = closestIntersection(scene, ray)) {
        InfinityIntersection -> scene.background
        is ObjectIntersection -> shade(ray, i)
      }

  fun shade(ray: Ray, intersection: ObjectIntersection): ColorD {
    val material = intersection.obj.material
    val color = material.color.multiply(scene.ambientBrightness)

    for (light in scene.lights) {
      if (!shadowed(intersection, light)) {
        color += material.color.multiply(localIntensity(light, intersection))
      }
    }

    return color
  }

  private fun shadowed(point: ObjectIntersection, light: Light): Boolean {
    val origin = point.hitPoint + (point.hitNormal * Eps)
    return when (light) {
      is PointLight, is SpotLight -> {
        val probe = Ray(origin, light.pos)
        when (val i = closestIntersection(scene, probe)) {
          is ObjectIntersection -> i.t > (light.pos - point.hitPoint).length
          is InfinityIntersection -> false
        }
      }
      is DirectedLight -> {
        false
        val probe = Ray(origin, origin + light.to)
        when (closestIntersection(scene, probe)) {
          is ObjectIntersection -> true
          is InfinityIntersection -> false
        }
      }
    }
  }

  fun localIntensity(light: Light, intersection: ObjectIntersection): Double =
      when (light) {
        is PointLight -> {
          diffuseIntensity(light, intersection) * distanceFalloff(light, intersection)
        }
        is SpotLight -> {
          diffuseIntensity(light, intersection) * distanceFalloff(light, intersection) * coneFalloff(light, intersection)
        }
        is DirectedLight -> {
          diffuseIntensity(light, intersection) * light.brightness
        }
      }

  private fun distancedDiffuseIntensity(light: Light, intersection: ObjectIntersection): Double {
    val distance = (light.pos - intersection.hitPoint).length
    return diffuseIntensity(light, intersection) * (light.brightness / (PI * distance * distance))
  }

  private fun diffuseIntensity(light: Light, intersection: ObjectIntersection) =
    max(0.0, intersection.hitNormal.dot(toLight(light, intersection))) * intersection.obj.material.diffuse

  private fun distanceFalloff(light: Light, intersection: ObjectIntersection): Double {
    val distance = (light.pos - intersection.hitPoint).length
    return light.brightness / (PI * distance * distance)
  }

  private fun coneFalloff(light: SpotLight, intersection: ObjectIntersection): Double {
    val spotAngle = light.spread
    val hitPointAngle = acos(light.direction.dot(-toLight(light, intersection)))

    return if (hitPointAngle in 0.0..spotAngle) {
      val falloff = 1 - (hitPointAngle / spotAngle).pow(2)
      distancedDiffuseIntensity(light, intersection) * falloff
    } else {
      0.0
    }
  }

  fun toLight(light: Light, intersection: ObjectIntersection): VectorD =
    when (light) {
      is PointLight, is SpotLight -> (light.pos - intersection.hitPoint).unit
      is DirectedLight -> light.to
    }

  fun reflection(light: ColorD, material: ColorD, k: Double, shadowAttenuation: Double): ColorD {
    val color: ColorD = light.copy()
    color *= material
    color *= k * shadowAttenuation
    return color
  }
}
