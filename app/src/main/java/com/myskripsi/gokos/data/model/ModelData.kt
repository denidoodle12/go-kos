package com.myskripsi.gokos.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Campus (
    val id: String = "",
    val nama_kampus: String = "", // Sesuaikan dengan field di Firestore
    val alamat: String = "",    // Sesuaikan dengan field di Firestore
    val logo_url: String = "",  // Sesuaikan dengan field di Firestore
    val lokasi: Lokasi = Lokasi(), // Sesuaikan dengan field di Firestore
    val radius: Int = 0
) : Parcelable

@Parcelize
data class Kos (
    val id: String = "",
    val nama_kost: String = "",
    val alamat: String = "",
    val deskripsi: String = "",
    val foto_kost: List<String> = listOf(),
    val harga: Int = 0,
    val kampus_terdekat: String = "",
    val kategori: String = "",
    val lokasi: Lokasi = Lokasi(), // Perhatikan, ini adalah Lokasi custom Anda
    val fasilitas_kamar: List<String> = listOf(),
    val fasilitas_kamar_mandi: List<String> = listOf(),
    val listrik: String = ""
) : Parcelable

@Parcelize
data class Location( // Untuk koordinat umum, seperti pada Kampus
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable

@Parcelize
data class Lokasi( // Untuk data lokasi pada Kos, termasuk jarak
    var jarak: Double = 0.0, // Ubah jadi var agar bisa di-update
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable