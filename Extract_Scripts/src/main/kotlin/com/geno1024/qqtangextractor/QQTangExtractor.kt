package com.geno1024.qqtangextractor

import com.geno1024.qqtangextractor.ds.IMG

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
//            GBK12("/res/GBK12.bmp").toTTF()
            IMG("/object/ui/login/img_logo.img").decode()
        }
    }
}
