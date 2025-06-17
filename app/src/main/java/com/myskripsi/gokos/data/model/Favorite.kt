// File: data/model/Favorite.kt
package com.myskripsi.gokos.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Favorite(
    var id: String = "",
    var userId: String = "",
    var kosId: String = "",
    var note: String? = null,
    @ServerTimestamp
    var timestamp: Date? = null
) {
    // Constructor kosong untuk Firestore
    constructor() : this("", "", "", null, null)
}