package com.example.newsapp.util

sealed class Resource <T>(
    val data:T?=null,
    val messgae:String?=null
    ){
    class Success<T>(data: T?):Resource<T>(data)
    class error<T>(messgae: String,data: T?=null):Resource<T>(data,messgae)
    class loading<T>:Resource<T>()
}