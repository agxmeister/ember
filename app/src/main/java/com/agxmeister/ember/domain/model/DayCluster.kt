package com.agxmeister.ember.domain.model

enum class DayCluster(val label: String, val description: String) {
    Eos("Eos", "You usually weigh yourself in the early morning. Eos is the goddess of dawn."),
    Helios("Helios", "You usually weigh yourself in the late morning. Helios is the god of the sun."),
    Hesperus("Hesperus", "You usually weigh yourself in the afternoon or evening. Hesperus is the evening star."),
    Selene("Selene", "You usually weigh yourself at night. Selene is the goddess of the moon."),
}
