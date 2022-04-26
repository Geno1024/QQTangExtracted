package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.utils.GBK12

object QQTangExtractor
{
    fun help()
    {

    }

    @JvmStatic
    fun main(args: Array<String>)
    {
        if (args.isEmpty())
        {
            help()
        }
        else
        {
            Settings.base = args[0]
            GBK12("/res/GBK12.bmp").toTTF()
        }
    }
}
