package cc.cheatex.ktracer

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

fun parseScene(input: InputStream): Scene {
    val reader = BufferedReader(InputStreamReader(input))

    val background = readColor(reader)
    val ambientLight = readColor(reader)
    val camera = readCamera(reader)
    val lights = readLights(reader)
    val objects = readObjects(reader)

   return Scene(camera, objects, lights, background, ambientLight)
}

private fun readCamera(reader: BufferedReader): Camera {
    val name = reader.readLine().trim()
    val at = readVector(reader)
    val up = readVector(reader)
    val viewport = reader.readLine().trim().toDouble()

    return Camera(at, up, viewport)
}

private fun readLights(reader: BufferedReader): List<Light> =
    readObjectGroup(reader, ::readPointLight) +
    readObjectGroup(reader, ::readSpotLight)

private fun readObjects(reader: BufferedReader): List<MaterialObject> =
    readObjectGroup(reader, ::readSphere)

private fun readPointLight(reader: BufferedReader): PointLight {
    val name = reader.readLine().trim()
    val position = readVector(reader)
    val intensity = readColor(reader)

   return PointLight(position, intensity)
}

private fun readSpotLight(reader: BufferedReader): SpotLight {
    val name = reader.readLine().trim()
    val position = readVector(reader)
    val at = readVector(reader)
    val angle = reader.readLine().trim().toDouble()
    val intensity = readColor(reader)

   return SpotLight(position, at, angle, intensity)
}

private fun <T, R : T> readObjectGroup(reader: BufferedReader,
                               objectReader: (BufferedReader) -> R): List<T> {
    reader.readLine()
    val objectsCount = reader.readLine().trim().toInt()

    return sequence {
        for (i in 0..objectsCount) yield(objectReader(reader))
    } .toList()
}

private fun readSphere(reader: BufferedReader): Sphere {
    val name = reader.readLine().trim()
    val position = readVector(reader)
    val radius = reader.readLine().trim().toDouble()
    val material = readMaterial(reader)

   return Sphere(position, material, radius)
}

private fun readMaterial(reader: BufferedReader): Material {
    return Material(ColorD.white, ColorD.white, ColorD.white, 1.0, 1.0, 1.0, 1.0, 1.0)
}

private fun readVector(reader: BufferedReader): VectorD {
    val tokenizer = StringTokenizer(reader.readLine())
    return VectorD(tokenizer.nextToken().toDouble(),
            tokenizer.nextToken().toDouble(),
            tokenizer.nextToken().toDouble())
}

private fun readColor(reader: BufferedReader): ColorD {
    val tokenizer = StringTokenizer(reader.readLine())
    return ColorD(tokenizer.nextToken().toDouble(),
            tokenizer.nextToken().toDouble(),
            tokenizer.nextToken().toDouble())
}
