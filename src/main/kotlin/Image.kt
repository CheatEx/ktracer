package cc.cheatex.ktracer

typealias Image = Array<Array<ColorD>>

fun normalize(image: Image) {
    var maxValue = 1.0
    var minValue = 0.0
    applyForComponents(image) { c ->
        if (c > maxValue) maxValue = c
        if (c < minValue) minValue = c
    }

    val norm: Double = - minValue + maxValue
    processPixels(image) { pixel ->
        pixel -= minValue
        pixel /= norm
    }
}

fun processPixels(image: Image, func: (ColorD) -> Unit) {
    for (column in image)
        for (pixel in column)
            func(pixel)
}

fun applyForComponents(image: Image, func: (Double) -> Unit) {
    for (column in image)
        for (pixel in column)
            for (component in pixel.array)
                func(component)
}
