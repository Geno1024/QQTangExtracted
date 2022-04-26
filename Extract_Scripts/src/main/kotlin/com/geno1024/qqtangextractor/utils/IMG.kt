package com.geno1024.qqtangextractor.utils

import com.geno1024.qqtangextractor.Settings
import java.io.File

/**
 * Unpack ${Settings.base}/[path] IMG file.
 */
class IMG(val path: String)
{
    val file = File("${Settings.base}$path")

    fun getImageData()
    {

    }
}
