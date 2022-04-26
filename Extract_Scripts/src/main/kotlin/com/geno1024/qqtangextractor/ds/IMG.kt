package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.utils.toInt32
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

/**
 * Unpack ${Settings.base}/[path] IMG file.
 */
class IMG(val path: String)
{
    val TAG = "[IMG     ]"

    val file = File("${Settings.base}$path").apply { if (Settings.debug) println("$TAG Handling $this.") }
    val stream = file.inputStream()

    val ds = DS(stream)

    class DS(stream: FileInputStream)
    {
        val magic = stream.readNBytes(8).toString(Charset.defaultCharset())
        val version = stream.readNBytes(4).toInt32()
        val colorDepth = stream.readNBytes(4).toInt32()
        val framesSize = stream.readNBytes(4).toInt32()
        val frameGroups = stream.readNBytes(4).toInt32()
        val unknown1 = stream.readNBytes(4).toInt32()
        val unknown2 = stream.readNBytes(4).toInt32()
        val width = stream.readNBytes(4).toInt32()
        val height = stream.readNBytes(4).toInt32()
        val frames = (0..framesSize).map { Frame(stream) }
    }

    class Frame(stream: FileInputStream)
    {
        val magic = stream.readNBytes(4).toInt32()
        val centerX = stream.readNBytes(4).toInt32()
        val centerY = stream.readNBytes(4).toInt32()
        val imageType = stream.readNBytes(4).toInt32()
        val width = stream.readNBytes(4).toInt32()
        val height = stream.readNBytes(4).toInt32()
    }

    fun decode()
    {
        println(ds.frames[0].imageType)
    }
}
