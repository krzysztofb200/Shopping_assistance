package com.example.shopping_assistance.ui

data class BarcodeClass(
    val id: String = "", // Unikalny identyfikator kodu kreskowego
    val name: String = "", // Nazwa kodu kreskowego
    val value: String = "" // Wartość kodu kreskowego
) {
    constructor(id: String, name: String) : this()
}