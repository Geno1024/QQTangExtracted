package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.versions.QQTang5211

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
            Settings.version = args[0]
            Settings.base = args[1]
            when (Settings.version)
            {
                "QQTang5.2_Beta1Build1" -> QQTang5211()
            }
//            GBK12("/res/GBK12.bmp").toTTF()
//            IMG("/object/ui/login/img_logo.img").decode()
        }
    }
}
