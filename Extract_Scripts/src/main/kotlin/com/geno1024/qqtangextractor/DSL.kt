package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.ds.IMG2
import com.geno1024.qqtangextractor.ds.PKG
import java.io.File

abstract class Inst(open val opr: MutableList<String>)
{
    open operator fun invoke() {}

//    infix fun withIdx(s: String) = apply { opr.add(s) }

    infix fun decode(type: String) = when (type)
    {
        "IMG" -> IMGInst(opr)()
        "PKG" -> PKGInst(opr)()
        else -> NOPInst()()
    }

    class IMGInst(override val opr: MutableList<String>) : Inst(opr)
    {
        override fun invoke() = opr.forEach { IMG2(it).decode() }
    }

    class PKGInst(override val opr: MutableList<String>) : Inst(opr)
    {
        override fun invoke() = PKG(opr[1], opr[0]).extract()
    }

    class NOPInst() : Inst(mutableListOf())
    {
        override fun invoke() {}
    }
}

infix fun String.copyTo(target: String) = File("${Settings.base}/$this").copyRecursively(File("${Settings.version}/$target"), overwrite = true)

infix fun String.decode(type: String) = when (type)
{
    "IMG" -> Inst.IMGInst(mutableListOf(this)).apply(Inst.IMGInst::invoke)
    "PKG" -> Inst.PKGInst(mutableListOf(this))
    else -> Inst.NOPInst()
}

infix fun String.withIndex(file: String) = Inst.PKGInst(mutableListOf(this, file))

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
