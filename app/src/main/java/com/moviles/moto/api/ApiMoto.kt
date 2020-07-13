package com.moviles.moto.api

import com.moviles.moto.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiMoto {

    @GET("motos/{id}")
    suspend fun getBike(@Path("id") id:Int):ResponseGeneric<Moto>

    @GET("pedidos/{id}")
    suspend fun getPedidoDetalle(@Path("id") id: Int): ResponseGeneric<Pedido>

    @POST("motos/{id}")
    fun sendBikeLocation(@Path("id") id:Int):ResponseGeneric<String>

    @POST("usuarios/login")
    suspend fun login(@Body usuario: Usuario): ResponseGeneric<ArrayList<Usuario>>

    @POST("pedidos/{id}/entregado")
    fun changeState(@Path("id") id: Int):Call<ResponseGeneric<Pedido>>

    @POST("motos/{id}/ubicacion")
    fun updateLocation(@Path("id")id :Int, @Body ubicacion: Ubicacion?):Call<ResponseGeneric<Moto>>

}