package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.ds.IMG
import java.io.File

infix fun String.copyTo(target: String) = File("${Settings.base}/$this").copyRecursively(File("${Settings.version}/$target"), overwrite = true)

infix fun String.decode(type: String) = when (type)
{
    "IMG" -> IMG(this).decode()
    else -> {}
}

infix fun String.decodeFiles(type: String) = when (type)
{
    "IMG" -> File("${Settings.base}/$this")
        .listFiles { pathname ->
            pathname.isFile and (pathname.extension == "img")
        }?.forEach {
            "/${it.toRelativeString(File(Settings.base)).replace("\\", "/")}" decode "IMG"
        }
    else -> {}
}
