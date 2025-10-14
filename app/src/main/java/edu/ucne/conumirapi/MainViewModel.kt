package edu.ucne.conumirapi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class UiState(
    val titulo: String = "",
    val descripcion: String = "",
    val nivel: Int = 2,
    val fechaVencimiento: String = "",
    val completada: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val lista: List<Prioridad> = emptyList()
)

class MainViewModel : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    private val api = NetworkModule.api

    init { cargar() }

    fun setTitulo(v: String) = _ui.tryEmit(_ui.value.copy(titulo = v))
    fun setDescripcion(v: String) = _ui.tryEmit(_ui.value.copy(descripcion = v))
    fun setNivel(v: Int) = _ui.tryEmit(_ui.value.copy(nivel = v))
    fun setFecha(v: String) = _ui.tryEmit(_ui.value.copy(fechaVencimiento = v))
    fun setCompletada(v: Boolean) = _ui.tryEmit(_ui.value.copy(completada = v))

    private fun friendlyError(e: Throwable) = when (e) {
        is UnknownHostException -> "No se pudo conectar al servidor. Revisa tu internet o la URL."
        is SocketTimeoutException -> "El servidor tardó en responder. Intenta de nuevo."
        is ConnectException -> "No hay conexión con el servidor."
        else -> "Ocurrió un error inesperado."
    }

    fun cargar() = viewModelScope.launch {
        try {
            _ui.emit(_ui.value.copy(cargando = true, error = null))
            val data = api.getPrioridades()
            _ui.emit(_ui.value.copy(lista = data, cargando = false))
        } catch (e: Exception) {
            _ui.emit(_ui.value.copy(error = friendlyError(e), cargando = false))
        }
    }

    fun registrar() = viewModelScope.launch {
        val s = _ui.value
        if (s.titulo.isBlank()) {
            _ui.emit(s.copy(error = "El título es obligatorio"))
            return@launch
        }
        val body = Prioridad(
            titulo = s.titulo.trim(),
            descripcion = s.descripcion.trim().ifBlank { null },
            nivel = s.nivel,
            fechaVencimiento = s.fechaVencimiento.trim().ifBlank { null },
            completada = s.completada
        )
        try {
            _ui.emit(s.copy(cargando = true, error = null))
            api.postPrioridad(body)
            val data = api.getPrioridades()
            _ui.emit(s.copy(
                titulo = "", descripcion = "", nivel = 2,
                fechaVencimiento = "", completada = false,
                lista = data, cargando = false, error = null
            ))
        } catch (e: Exception) {
            _ui.emit(s.copy(error = friendlyError(e), cargando = false))
        }
    }
}
