package edu.ucne.conumirapi

data class Prioridad(
    val id: Int? = null,
    val titulo: String,
    val descripcion: String? = null,
    val nivel: Int = 2,
    val fechaVencimiento: String? = null,
    val completada: Boolean = false,
    val creadaEl: String? = null,
    val actualizadaEl: String? = null
)
