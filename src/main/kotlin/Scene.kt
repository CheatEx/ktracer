package cc.cheatex.ktracer

import kotlin.math.PI

class Material(val Ka: VectorD,
               val Kd: VectorD,
               val Ks: VectorD,
               val alpha: Double,
               val rI: Double,
               val rC: Double,
               val tC: Double,
               val lC: Double)

sealed class SceneObject(val pos: VectorD)

sealed class DirectedSceneObject(pos: VectorD, val at: VectorD) : SceneObject(pos)

sealed class MaterialObject(pos: VectorD, val material: Material) : SceneObject(pos)

class Sphere(pos: VectorD, material: Material, val radius: Double) : MaterialObject(pos, material)

sealed class Light(pos: VectorD, val intensity: VectorD) : SceneObject(pos)

class PointLight(pos: VectorD, intensity: VectorD) : Light(pos, intensity)

class SpotLight(pos: VectorD, val at: VectorD, degreesAngle: Double, intensity: VectorD)
    : Light(pos, intensity) {
    val angle = degreesAngle * PI / 180
}

class Camera(at: VectorD, upo: VectorD, val viewport: Double)
    : DirectedSceneObject(VectorD.zero, at) {
    val up = upo.norm
}

class Scene(
        val camera: Camera,
        val objects: List<MaterialObject>,
        val lights: List<Light>,
        val ambientLight: VectorD,
        val background: ColorD)

