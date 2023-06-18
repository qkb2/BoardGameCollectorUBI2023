package edu.put.inf151825

fun Boolean.toInt() = if (this) 1 else 0

fun stringCutter(src: String): String {
    var res = ""
    var lim = 10
    for (i in src.indices) {
        if (src[i] != ' ') {
            res += src[i]
        } else {
            if (i >= lim) {
                res += '\n'
                lim += i
            } else {
                res += ' '
            }
        }
    }
    return res
}
