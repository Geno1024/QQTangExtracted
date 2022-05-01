package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.ds.IMG2
import java.io.File

infix fun String.copyTo(target: String) = File("${Settings.base}/$this").copyRecursively(File("${Settings.version}/$target"), overwrite = true)

infix fun String.decode(type: String) = when (type)
{
    "IMG" -> IMG2(this).decode()
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
