package com.example.gestrestauhco

object YuvToRgbConverter {
    fun nv21ToRgb(nv21: ByteArray, width: Int, height: Int, out: IntArray) {
        var i = 0
        var yIndex = 0
        val frameSize = width * height

        while (i < frameSize) {
            val y = nv21[yIndex].toInt() and 0xff
            val v = nv21[frameSize + (yIndex / 2) * 2].toInt() and 0xff
            val u = nv21[frameSize + (yIndex / 2) * 2 + 1].toInt() and 0xff

            var r = y + (1.370705 * (v - 128)).toInt()
            var g = y - (0.337633 * (u - 128) + 0.698001 * (v - 128)).toInt()
            var b = y + (1.732446 * (u - 128)).toInt()

            r = r.coerceIn(0, 255)
            g = g.coerceIn(0, 255)
            b = b.coerceIn(0, 255)

            out[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
            i++
            yIndex++
        }
    }
}
