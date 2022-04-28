package com.geno1024.qqtangextractor.versions

import com.geno1024.qqtangextractor.Settings
import com.geno1024.qqtangextractor.ds.IMG
import com.geno1024.qqtangextractor.ds.ResourceTree
import java.io.File

object QQTang5211
{
    operator fun invoke()
    {
        File("${Settings.base}/music").copyRecursively(File("${Settings.version}/music"), overwrite = true)
        IMG("/object/ui/login/img_logo.img").decode()
        File("${Settings.base}/sound").copyRecursively(File("${Settings.version}/sound"), overwrite = true)
        File("${Settings.version}/README.md").writeText(ResourceTree.toTreeDiagram())
    }
}
