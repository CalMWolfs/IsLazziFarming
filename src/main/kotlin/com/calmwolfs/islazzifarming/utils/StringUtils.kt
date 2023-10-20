package com.calmwolfs.islazzifarming.utils

import java.text.NumberFormat

object StringUtils {
    fun Number.addSeparators(): String = NumberFormat.getNumberInstance().format(this)

    fun String.unformat(): String {
        val builder = StringBuilder()

        var counter = 0
        while (counter < this.length) {
            if (this[counter] == '§') {
                counter += 2
            } else {
                builder.append(this[counter])
                counter++
            }
        }
        return builder.toString()
    }
}