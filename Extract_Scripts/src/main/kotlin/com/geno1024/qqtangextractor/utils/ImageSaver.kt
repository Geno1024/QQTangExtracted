package com.geno1024.qqtangextractor.utils

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadataNode

object ImageSaver
{
    fun savePNG(frame: BufferedImage, target: String) = ImageIO.write(
        frame,
        "PNG",
        File(target)
    )

    fun saveGIF(frames: List<BufferedImage>, target: String, delayMilliseconds: Int = 500) = ImageIO.getImageWritersByFormatName("GIF")
        .next()
        .apply {
            val metadata = getDefaultImageMetadata(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB),
                null
            ).apply {
                setFromTree(
                    nativeMetadataFormatName,
                    getAsTree(nativeMetadataFormatName).apply {
                        appendChild(IIOMetadataNode("GraphicControlExtension").apply {
                            setAttribute("delayTime", (delayMilliseconds / 10).toString())
                            setAttribute("disposalMethod", "restoreToBackgroundColor")
                            setAttribute("transparentColorFlag", "TRUE")
                            setAttribute("transparentColorIndex", "0")
                            setAttribute("userInputFlag", "FALSE")
                        })
                        appendChild(IIOMetadataNode("ApplicationExtensions").apply {
                            appendChild(IIOMetadataNode("ApplicationExtension").apply {
                                setAttribute("applicationID", "NETSCAPE")
                                setAttribute("authenticationCode", "2.0")
                                userObject = byteArrayOf(0x1, 0, 0)
                            })
                        })
                    }
                )
            }
            output = ImageIO.createImageOutputStream(File(target))
            prepareWriteSequence(null)
            frames.forEach {
                writeToSequence(IIOImage(it, null, metadata), null)
            }
            endWriteSequence()
        }
}
