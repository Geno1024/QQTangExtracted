package com.geno1024.qqtangextractor.utils

import java.io.File
import javax.imageio.ImageIO

/**
 * Convert [path]/res/GBK12.bmp into a .ttf file.
 *
 * @author Geno1024
 */
class GBK12(val path: String)
{
    val size = 12

    val targetFile = File("$path/res/GBK12.bmp")
    val image = ImageIO.read(targetFile)

    fun getImageFromCodepoint(gbk: Int) = image.getSubimage((gbk % (image.width / size)) * size, (gbk / (image.width / size)) * size, size, size)

    fun toTTF()
    {
        File("./temp").mkdir()
        (0 until (image.width / size) * (image.height / size)).map {
            ImageIO.write(getImageFromCodepoint(it), "png", File("./temp/$it.png"))
        }
    }
}
