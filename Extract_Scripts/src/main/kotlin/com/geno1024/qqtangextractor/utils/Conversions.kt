package com.geno1024.qqtangextractor.utils

fun ByteArray.toInt32() = (this[0].toUByte().toUInt() + (this[1].toUByte().toUInt() shl 8) + (this[2].toUByte().toUInt() shl 16) + (this[3].toUByte().toUInt() shl 24)).toInt()

fun ByteArray.toInt16() = (this[0].toUByte().toUInt() + (this[1].toUByte().toUInt() shl 8)).toInt()
