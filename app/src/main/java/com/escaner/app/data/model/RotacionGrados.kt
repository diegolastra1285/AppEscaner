package com.escaner.app.data.model

enum class RotacionGrados(val grados: Int) {
    ROT_0(0),
    ROT_90(90),
    ROT_180(180),
    ROT_270(270);

    fun rotarHorario(): RotacionGrados {
        val siguienteGrados = (grados + 90) % 360
        return entries.first { it.grados == siguienteGrados }
    }
}
