package cc.cheatex.ktracer

import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.Image as AwtImage

class STracerFrame : JFrame() {
    fun show(image: AwtImage, width: Int, height: Int) {
        contentPane.add(
                ImagePanel(image, width, height),
                BorderLayout.CENTER)
        setSize(width + 6, height + 30)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isResizable = false
        isVisible = true
    }
}

class ImagePanel(val img: AwtImage, val w: Int, val h: Int) : JPanel() {
    override fun paint(g: Graphics) {
        g.drawImage(img, 0, 0, w, h, this)
    }
}

fun toAwtImage(image: Image): AwtImage {
    val width = image[0].size
    val height = image.size
    val awtImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (row in 0.until(height))
        for (column in 0.until(width))
            awtImage.setRGB(column, row, image[row][column].rgbInt())

    return awtImage
}
