package com.moviles.moto.models

import java.io.Serializable

data class Pedido(
    val id:Int,
    val usuario_id: Int,
    val restaurante_id: Int,
    val totalAPagar: Double,
    val fechaHora: String,
    val direccionEntrega:String,
    val latitud: String,
    val longitud: String,
    val estado: String,
    val restaurante: Restaurant,
    val moto_id:Int?
) : Serializable