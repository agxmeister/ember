package com.agxmeister.ember.domain.model

enum class WeightUnit {
    Kg, Lbs;

    val label: String get() = name.lowercase()

    val displayRange: IntRange
        get() = when (this) {
            Kg -> 30..300
            Lbs -> 66..661
        }

    val step: Double
        get() = when (this) {
            Kg -> 0.1
            Lbs -> 0.5
        }

    fun fromKg(kg: Double): Double = when (this) {
        Kg -> kg
        Lbs -> kg * 2.20462
    }

    fun toKg(value: Double): Double = when (this) {
        Kg -> value
        Lbs -> value / 2.20462
    }

    fun scaleDiff(kgDiff: Double): Double = when (this) {
        Kg -> kgDiff
        Lbs -> kgDiff * 2.20462
    }
}
