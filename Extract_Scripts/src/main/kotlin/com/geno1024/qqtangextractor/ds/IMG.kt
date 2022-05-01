@file:Suppress("unused")

package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.utils.toInt32
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadataNode

/**
 * Unpack ${Settings.base}/[path] IMG file.
 */
class IMG(val path: String)
{
    val TAG = "[IMG     ]"

    val file = File("${Settings.base}$path").apply { if (Settings.debug) print("\n$TAG Handling $this: ") }
    val stream = file.inputStream()

    val ds = DS(stream)

    class DS(stream: FileInputStream)
    {
        val magic = stream.readNBytes(8).toString(Charset.defaultCharset())
        val version = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("version = $this, ") }
        val colorDepth = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("colorDepth = $this, ") }
        val framesSize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("framesSize = $this, ") }
        val frameGroups = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("frameGroups = $this, ") }
        val offsetX = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetX = $this, ") }
        val offsetY = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetY = $this, ") }
        val width = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width = $this, ") }
        val height = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height = $this.") }
//        val frames = Frame(stream)
        val frames = (0 until framesSize).map { Frame(stream) }
    }

    class Frame(stream: FileInputStream)
    {
        val magic = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("\n\t[IMGFrame] Subimage: ") }
        val offsetX = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetX = $this, ") }
        val offsetY = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetY = $this, ") }
        val imageType = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("imageType = $this, ") }
        val width = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width = $this, ") }
        val height = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height = $this, ") }
        val widthBytes = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("widthBytes? = $this.") }

        val data = when (imageType)
        {
            3 -> Type3Data(stream.readNBytes(width * height * 3), width, height)
            8 -> Type8Data(stream.readNBytes(width * height * 4), width, height)
            else -> TypeEData()
        }
    }

    interface FrameData

    class Type3Data(bytes: ByteArray, width: Int, height: Int): FrameData
    {
        val color = bytes.toList().subList(0, width * height * 2).windowed(2, 2).map {
            val rgb565 = (it[1].toUByte().toUInt() shl 8) + it[0].toUByte().toUInt()
            val rgb888 = ((rgb565 and 0b11111_000000_00000u) shl 8) + ((rgb565 and 0b00000_111111_00000u) shl 5) + ((rgb565 and 0b00000_000000_11111u) shl 3)
            rgb888
        }
        val alpha = bytes.toList().subList(width * height * 2, width * height * 3).map { (it.toInt() shl 3).takeUnless { it == 256 }?:255 }
    }

    class Type8Data(bytes: ByteArray, width: Int, height: Int): FrameData
    {
        val color = bytes.toList().subList(0, width * height * 4).windowed(4, 4).map {
            (it[3].toUByte().toUInt() shl 24) + (it[2].toUByte().toUInt() shl 16) + (it[1].toUByte().toUInt() shl 8) + it[0].toUByte().toUInt()
        }
//        val alpha = bytes.toList().subList(width * height * 3, width * height * 4).map { (it.toInt() shl 3).takeUnless { it == 256 }?:255 }
    }
    class TypeEData(): FrameData

    fun decode()
    {
        File("${Settings.version}${path.substringBeforeLast('/')}").mkdirs()
        when
        {
            ds.framesSize == 1 ->
            {
                val frame = ds.frames[0]
                ImageIO.write(
                    when (frame.data)
                    {
                        is Type3Data ->
                        {
                            BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB).apply {
                                (0 until frame.width).map { x ->
                                    (0 until frame.height).map { y ->
                                        setRGB(x, y, (frame.data.alpha[y * frame.width + x] shl 24) + frame.data.color[y * frame.width + x].toInt())
                                    }
                                }
                            }
                        }
                        is Type8Data ->
                        {
                            BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB).apply {
                                (0 until frame.width).map { x ->
                                    (0 until frame.height).map { y ->
                                        setRGB(x, y, frame.data.color[y * frame.width + x].toInt())
                                    }
                                }
                            }
                        }
                        else ->
                        {
                            BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB)
                        }
                    },
                    "PNG",
                    File("${Settings.version}${path.substringBeforeLast('.')}.png")
                )
            }
            ds.frameGroups == 1 ->
            {
                ImageIO.getImageWritersByFormatName("GIF").next().apply {
                    val metadata = getDefaultImageMetadata(
                        ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB),
                        null
                    ).apply {
                        setFromTree(
                            nativeMetadataFormatName,
                            getAsTree(nativeMetadataFormatName).apply {
                                appendChild(IIOMetadataNode("GraphicControlExtension").apply {
                                    setAttribute("delayTime", "50")
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
                    output = ImageIO.createImageOutputStream(File("${Settings.version}${path.substringBeforeLast('.')}.gif"))
                    prepareWriteSequence(null)
                    ds.frames.forEach {
                        when (it.data)
                        {
                            is Type3Data ->
                            {
                                writeToSequence(
                                    IIOImage(
                                        BufferedImage(ds.width, ds.height, BufferedImage.TYPE_INT_ARGB).apply {
                                            (0 until it.width).map { x ->
                                                (0 until it.height).map { y ->
                                                    setRGB(x - ds.offsetX + it.offsetX, y - ds.offsetY + it.offsetY, (it.data.alpha[y * it.width + x] shl 24) + it.data.color[y * it.width + x].toInt())
                                                }
                                            }
                                        },
                                        null,
                                        metadata
                                    ),
                                    null
                                )
                            }
                            else -> {}
                        }
                    }
                    endWriteSequence()
                }
            }
        }
    }
}
