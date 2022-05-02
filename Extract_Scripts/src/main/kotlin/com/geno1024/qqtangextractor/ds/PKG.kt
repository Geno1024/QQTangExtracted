@file:Suppress("unused")

package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.utils.toInt16
import com.geno1024.qqtangextractor.utils.toInt32
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.Inflater

class PKG(idx: String, val pkg: String)
{
    val TAG = "[PKG     ]"

    val index = File("${Settings.base}$idx") // .apply { if (Settings.debug) print("\n$IDX Handling index $this: ") }
    val indexStream = index.inputStream()

    val pack = File("${Settings.base}$pkg").apply { if (Settings.debug) print("\n$TAG Handling package $this with index $index: ") }
//    val packStream = pack.inputStream()

    val indexDS = IndexDS(indexStream)
    val pkgDS = PkgDS(indexDS, pack)

    class IndexDS(stream: FileInputStream)
    {
        val magic = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("magic = $this, ") }
        val entrySize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("entrySize = $this, ") }
        val fileTableOffset = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("fileTableOffset = $this, ") }
        val fileTableSize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("fileTableSize = $this, ") }
        val entries = (0 until entrySize).map { IndexEntry(stream) }
    }

    class IndexEntry(stream: FileInputStream)
    {
        val nameLength = stream.readNBytes(2).toInt16().apply { if (Settings.debug) print("\n\t[IDXEntry] nameLength = $this, ") }
        val name = stream.readNBytes(nameLength).toString(Charsets.UTF_8).apply { if (Settings.debug) print("name = $this, ") }
        val flag = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("flag = $this, ") }
        val offset = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("offset = $this, ") }
        val size = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("size = $this, ") }
        val compressedSize = stream.readNBytes(4).toInt32().apply { if (Settings.debug) print("compressedSize = $this.") }
    }

    class PkgDS(indexDS: IndexDS, pack: File)
    {
        val compressedData = indexDS.entries.map { entry ->
            if (Settings.debug) print("\n\t[PKGEntry] name = ${entry.name}")
            pack.inputStream().use { stream ->
                stream.skipNBytes(entry.offset.toLong())
                entry to stream.readNBytes(entry.size)
            }
        }
    }

    fun extract()
    {
        pkgDS.compressedData.forEach {
            val filename = it.first.name.replace("\\", "/")
            if (Settings.debug) print("\n\t[PKGUnzip] unzipping $filename.")
            val decompressed = with(Inflater()) {
                setInput(it.second)
                ByteArray(it.first.size).apply {
                    inflate(this)
                }
            }
            File("${Settings.base}${pkg.substringBeforeLast('/')}/${filename.substringBeforeLast('/')}").mkdirs()
            FileOutputStream("${Settings.base}${pkg.substringBeforeLast('/')}/$filename").write(decompressed)
        }
    }
}
