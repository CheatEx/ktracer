package cc.cheatex.ktracer

import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import java.io.FileInputStream
import java.io.IOException
import kotlin.system.exitProcess

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

  val options = RenderingOptions(Resolution(imageWidth, imageHeight), depth, minWeight, lightAttenuation)

  val parseResult: ParseResult<Scene> =
      try {
        FileInputStream(sceneFileName).use {
          SceneParser.tryParseToEnd(it)
        }
      } catch (io: IOException) {
        System.err.println("Can't read file $sceneFileName: ${io.message}")
        exitProcess(1)
      }

  when (parseResult) {
    is Parsed<Scene> -> runTracer(parseResult.value, options)
    is ErrorResult -> {
      System.err.println("Failed to parse scene: $parseResult")
      exitProcess(1)
    }
  }
}

fun runTracer(scene: Scene, options: RenderingOptions) {
  val tracer = Tracer(scene, options)
  val rawImage: Image = tracer.render()

  STracerFrame().show(toAwtImage(rawImage), options.resolution.width, options.resolution.height)
}
