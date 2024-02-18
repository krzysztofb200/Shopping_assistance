package com.example.shopping_assistance.ui.models

data class ShoppingListClass(
    val listId: String = "",  // ustaw wartość domyślną dla String
    val listName: String = ""
) {
    // Domyślny konstruktor bezargumentowy
    constructor() : this("")
}