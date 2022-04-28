package com.geno1024.qqtangextractor.ds

import com.geno1024.qqtangextractor.Settings
import java.io.File

object ResourceTree
{
    fun listFiles(file: File, tabDepth: Int = 0): String =
        "${"    ".repeat(tabDepth)}- [${if (File("${Settings.version}/${file.relativeTo(File(Settings.base))}").absoluteFile.parentFile.takeIf { it.exists() }?.listFiles { it -> it.nameWithoutExtension == file.nameWithoutExtension }?.isNotEmpty() == true) "X" else " "}] ${file.relativeTo(File(Settings.base))}\n${if (file.isDirectory) file.listFiles().joinToString(separator = "") { listFiles(it, tabDepth + 1) } else ""}"

    fun toTreeDiagram() = listFiles(File(Settings.base))
}
