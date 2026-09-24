package com.argomin.app.util

fun formatRupiah(amount: Number): String {
    val longVal = amount.toLong()
    val str = longVal.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        sb.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return "Rp " + sb.reverse().toString()
}

fun formatNumber(amount: Number): String {
    val longVal = amount.toLong()
    val str = longVal.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        sb.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            sb.append('.')
        }
    }
    return sb.reverse().toString()
}
