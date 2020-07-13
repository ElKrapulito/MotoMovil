package com.moviles.moto.models

import java.io.Serializable

data class Usuario(
    val id:Int,
    val usuario:String,
    val password:String?,
    val tipoUsuario: String?,
    val token:String?,
    val moto: Moto?
): Serializable