@file:Suppress("unused")

package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.utils.ImageSaver
import com.geno1024.qqtangextractor.utils.toInt32
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import kotlin.math.max

class IMG2(val path: String)
{
    val TAG = "[IMG     ]"

    val file = File("${Settings.base}$path").apply { if (Settings.debug) print("\n$TAG handling $this: ") }
    val stream = file.inputStream()

    val ds = DS(stream)

    class DS(stream: FileInputStream)
    {
        val magic = stream.readNBytes(8).toString(Charset.defaultCharset())
        val version = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("version = $this, ") }
        val colorDepth = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("colorDepth = $this, ") }
        val framesSize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("framesSize = $this, ") }
        val frameGroups = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("frameGroups = $this, ") }
        val offsetX = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetX = $this, ") }.takeIf { it !in listOf(0xCCCCCCCC.toInt(), 19005184) }?:0 // patch: /map/bun06_8.img
        val offsetY = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetY = $this, ") }.takeIf { it !in listOf(0xCCCCCCCC.toInt(), 150) }?:0 // patch: /map/bun06_8.img
        val width = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width = $this, ") }
        val height = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height = $this.") }

        val frames = (0 until framesSize).map { Frame(stream, width, height, offsetX, offsetY).data }
    }

    class Frame(stream: FileInputStream, imageWidth: Int, imageHeight: Int, val imageOffsetX: Int, val imageOffsetY: Int)
    {
        val magic = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("\n\t[IMGFrame] Subimage: ") }
        val offsetX = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetX = $this, ") }
        val offsetY = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offsetY = $this, ") }
        val imageType = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("imageType = $this, ") }
        val width = if (imageType != 0) stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width = $this, ") } else 0
        val height = if (imageType != 0) stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height = $this, ") } else 0
        val widthBytes = if (imageType != 0) stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("widthBytes? = $this.") } else 0

        val data = BufferedImage(
            max(imageWidth.takeIf { it !in listOf(0xCCCCCCCC.toInt(), 4) }?:width, width - imageOffsetX + offsetX), // patch: /map/bun06_8.img
            max(imageHeight.takeIf { it !in listOf(0xCCCCCCCC.toInt(), 1792) }?:height, height -imageOffsetY + offsetY), // patch: /map/bun06_8.img
            BufferedImage.TYPE_INT_ARGB
        ).apply {
            setRGB(0, 0, width, height, IntArray(width * height) { 0x00000000 }, 0, width)
            val matrix = when (imageType)
            {
                3, 285212672 ->
                {
                    val bytes = stream.readNBytes(this@Frame.width * this@Frame.height * 3)
                    val rgb565 = bytes.toList()
                        .subList(0, this@Frame.width * this@Frame.height * 2)
                        .windowed(2, 2)
                        .map {
                            (it[1].toUByte().toUInt() shl 8) + it[0].toUByte().toUInt()
                        }
                    val rgb888 = rgb565.map {
                        ((it and 0b11111_000000_00000u) shl 8) +
                            ((it and 0b00000_111111_00000u) shl 5) +
                            ((it and 0b00000_000000_11111u) shl 3)
                    }
                    val alpha = bytes.toList()
                        .subList(this@Frame.width * this@Frame.height * 2, this@Frame.width * this@Frame.height * 3)
                        .map {
                            (it.toInt() shl 3).takeUnless { it == 256 } ?: 255
                        }
                    List(rgb888.size) { (alpha[it] shl 24) + rgb888[it].toInt() }
                }
                8 ->
                {
                    val bytes = stream.readNBytes(this@Frame.width * this@Frame.height * 4)
                    val rgb888 = bytes.toList()
                        .subList(0, this@Frame.width * this@Frame.height * 4)
                        .windowed(4, 4)
                        .map {
                            (it[3].toUByte().toUInt() shl 24) +
                                (it[2].toUByte().toUInt() shl 16) +
                                (it[1].toUByte().toUInt() shl 8) +
                                it[0].toUByte().toUInt()
                        }
                    rgb888.map(UInt::toInt)
                }
                else ->
                {
                    listOf()
                }
            }

            setRGB(
                (-imageOffsetX + offsetX).takeIf { it >= 0 }?:0, // patch: /map/icon/machine.img
                -imageOffsetY + offsetY,
                this@Frame.width,
                this@Frame.height,
                matrix.toIntArray(),
                0,
                this@Frame.width
            )
        }
    }

    fun decode()
    {
        File("${Settings.version}${path.substringBeforeLast('/')}").mkdirs()
        when
        {
            ds.framesSize == 1 ->
                ImageSaver.savePNG(
                    ds.frames[0],
                    "${Settings.version}${path.substringBeforeLast('.')}.png"
                )
            ds.frameGroups == 1 ->
                ImageSaver.saveGIF(
                    ds.frames,
                    "${Settings.version}${path.substringBeforeLast('.')}.gif"
                )
        }
    }
}
