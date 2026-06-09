package com.agxmeister.ember.domain.util

internal fun List<Double>.median(): Double {
    val s = sorted()
    return if (s.size % 2 == 0) (s[s.size / 2 - 1] + s[s.size / 2]) / 2.0 else s[s.size / 2]
}
