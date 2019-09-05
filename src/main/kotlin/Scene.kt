package cc.cheatex.ktracer

data class Material(val color: ColorD,
                    val alpha: Double,
                    val reflection: Double)

sealed class SceneObject(open val pos: VectorD)

sealed class DirectedSceneObject(override val pos: VectorD, open val at: VectorD)
  : SceneObject(pos)

sealed class MaterialObject(override val pos: VectorD, open val material: Material)
  : SceneObject(pos)

data class Sphere(override val pos: VectorD, override val material: Material, val radius: Double)
  : MaterialObject(pos, material)

sealed class Light(override val pos: VectorD, open val color: ColorD)
  : SceneObject(pos)

data class PointLight(override val pos: VectorD, override val color: ColorD)
  : Light(pos, color)

data class SpotLight(override val pos: VectorD, val at: VectorD, val spread: Double, override val color: ColorD)
  : Light(pos, color)

data class Camera(override val at: VectorD, val upo: VectorD, val viewport: Double)
  : DirectedSceneObject(VectorD.zero, at) {
  val up = upo.unit
}

data class Scene(
    val camera: Camera,
    val objects: List<MaterialObject>,
    val lights: List<Light>,
    val ambientLight: ColorD,
    val background: ColorD)
