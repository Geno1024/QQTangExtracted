package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.utils.toInt32
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import javax.imageio.ImageIO

/**
 * Unpack ${Settings.base}/[path] IMG file.
 */
class IMG(val path: String)
{
    val TAG = "[IMG     ]"

    val file = File("${Settings.base}$path").apply { if (Settings.debug) print("$TAG Handling $this: ") }
    val stream = file.inputStream()

    val ds = DS(stream)

    class DS(stream: FileInputStream)
    {
        val magic = stream.readNBytes(8).toString(Charset.defaultCharset())
        val version = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("version $this, ") }
        val colorDepth = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("colorDepth $this, ") }
        val framesSize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("framesSize $this, ") }
        val frameGroups = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("frameGroups $this, ") }
        val unknown1 = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("unknown1 $this, ") }
        val unknown2 = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("unknown2 $this, ") }
        val width = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width $this, ") }
        val height = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height $this.") }
//        val frames = Frame(stream)
        val frames = (0 until framesSize).map { Frame(stream) }
    }

    class Frame(stream: FileInputStream)
    {
        val magic = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("\n\t[IMGFrame] Subimage: ") }
        val centerX = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("centerX $this, ") }
        val centerY = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("centerY $this, ") }
        val imageType = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("imageType $this, ") }
        val width = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("width $this, ") }
        val height = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("height $this, ") }
        val widthBytes = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("widthBytes? $this.") }

        val data = stream.readNBytes(width * height * 3)
        val red = data.toList().subList(0, width * height - 1)
        val green = data.toList().subList(width * height, 2 * width * height - 1)
        val blue = data.toList().subList(width * height, 3 * width * height - 1)
    }

    fun decode()
    {
        ds.frames.map {
            BufferedImage(it.width, it.height, BufferedImage.TYPE_INT_RGB).apply {
                (0..)
            }
        }
    }
}
