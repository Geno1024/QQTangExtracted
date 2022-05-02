package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import java.io.File

object ResourceTree
{
    val banned = listOf("map")

    fun listFiles(file: File, tabDepth: Int = 0): String =
        "${"    ".repeat(tabDepth)}- [${if (File("${Settings.version}/${file.relativeTo(File(Settings.base))}").absoluteFile.parentFile.takeIf { it.exists() }?.listFiles { it -> ((it.nameWithoutExtension == file.nameWithoutExtension) or (it.name.substringBeforeLast('_') == file.nameWithoutExtension)) and (file.extension !in banned) }?.isNotEmpty() == true) "X" else " "}] ${file.toRelativeString(File(Settings.base)).replace("\\", "/")}\n${if (file.isDirectory) file.listFiles()?.joinToString(separator = "") { listFiles(it, tabDepth + 1) } else ""}"

    fun toTreeDiagram() = listFiles(File(Settings.base))
}
