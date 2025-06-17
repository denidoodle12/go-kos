package com.myskripsi.gokos.data.model

data class FavoriteItemUI(
    val favorite: Favorite,
    val kos: Kos,
    var distance: Double = -1.0
)