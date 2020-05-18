package com.ehDev.imHere.data

import com.ehDev.imHere.R

object HelpMethodsForScheduleRecycler {
    //Возвращает айди картинки первой буквы названия пары
    fun getPrefixResId(prefix: Char): Int = when (prefix) {
        'А' -> R.drawable.a
        'Б' -> R.drawable.b
        'В' -> R.drawable.v
        'Г' -> R.drawable.g
        'Д' -> R.drawable.d
        'Е' -> R.drawable.e
        'Ж' -> R.drawable.j
        'З' -> R.drawable.z
        'И' -> R.drawable.i
        'К' -> R.drawable.k
        'Л' -> R.drawable.l
        'М' -> R.drawable.m
        'Н' -> R.drawable.n
        'О' -> R.drawable.o
        'П' -> R.drawable.p
        'Р' -> R.drawable.r
        'С' -> R.drawable.s
        'Т' -> R.drawable.t
        'У' -> R.drawable.u
        'Ф' -> R.drawable.f
        'Х' -> R.drawable.h
        'Ц' -> R.drawable.c
        'Ч' -> R.drawable.ch
        'Ш' -> R.drawable.sh
        'Щ' -> R.drawable.shya
        'Э' -> R.drawable.ee
        'Ю' -> R.drawable.yu
        'Я' -> R.drawable.ya
        else -> 0 //fixme ругается на аргумент эксепшн, поэтому ноль кину пока
    }

    fun getPrefix(string: String) : Char = string[0]
}