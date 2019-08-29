package cc.cheatex.ktracer

import java.io.FileInputStream

fun main(args: Array<String>) {
    if (args.size != 6) {
        println("Usage: <stracer-command> width height depth min-weight light-attenuation scene-file")
        return
    }
    val imageWidth = args[0].toInt()
    val imageHeight = args[1].toInt()
    val depth = args[2].toInt()
    val minWeight = args[3].toDouble()
    val	lightAttenuation = args[4].toBoolean()
    val sceneFileName = args[5]

    val inputStream = FileInputStream(sceneFileName)
    val scene = parseScene(inputStream)
    val options = RenderingOptions(Resolution(imageWidth, imageHeight), depth, minWeight, lightAttenuation)

    val tracer = Tracer(scene, options)
    val rawImage: Image = tracer.render()

    STracerFrame().show(toAwtImage(rawImage), imageWidth, imageHeight)
}
