package edu.ucne.conumirapi

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/prioridades/")
    suspend fun getPrioridades(): List<Prioridad>

    @POST("api/prioridades/")
    suspend fun postPrioridad(@Body prioridad: Prioridad): Prioridad
}
