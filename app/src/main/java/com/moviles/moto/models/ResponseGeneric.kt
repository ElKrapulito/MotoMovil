package com.moviles.moto.models

data class ResponseGeneric<T>(
    val res:String,
    val data:T,
    val message:String?
)