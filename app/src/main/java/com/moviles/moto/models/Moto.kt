package com.moviles.moto.models

import java.io.Serializable

data class Moto (
    val id:Int,
    val usuario_id:String,
    val latitudActual: String,
    val longitudActual: String,
    val estado: String,
    val pedidoAsignado: String?
):Serializable
