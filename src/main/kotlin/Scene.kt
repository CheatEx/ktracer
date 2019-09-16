package cc.cheatex.ktracer

data class Material(val color: ColorD,
                    val diffuse: Double)

sealed class SceneObject(open val pos: VectorD)

sealed class MaterialObject(override val pos: VectorD, open val material: Material)
  : SceneObject(pos)

data class Sphere(override val pos: VectorD, val radius: Double, override val material: Material)
  : MaterialObject(pos, material)

sealed class Light(override val pos: VectorD, open val brightness: Double)
  : SceneObject(pos)

data class PointLight(override val pos: VectorD, override val brightness: Double)
  : Light(pos, brightness)

data class SpotLight(override val pos: VectorD, val at: VectorD, val spread: Double, override val brightness: Double)
  : Light(pos, brightness) {
  val direction: VectorD = (at - pos).unit
}

data class DirectedLight(val at: VectorD, override val brightness: Double)
  : Light(VectorD.zero, brightness) {
  val to: VectorD = -at.unit
}

data class Camera(val at: VectorD, val upo: VectorD, val viewport: Double)
  : SceneObject(VectorD.zero) {
  val direction = at.unit
  val up = upo.unit
}

data class Scene(
    val camera: Camera,
    val objects: List<MaterialObject>,
    val lights: List<Light>,
    val ambientBrightness: Double,
    val background: ColorD)
