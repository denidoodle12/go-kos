package com.myskripsi.gokos.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Campus (
    val id: String = "",
    val nama_kampus: String = "",
    val alamat: String = "",
    val logo_url: String = "",
    val lokasi: Lokasi = Lokasi(),
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
    val lokasi: Lokasi = Lokasi(),
    val fasilitas_kamar: List<String> = listOf(),
    val fasilitas_kamar_mandi: List<String> = listOf(),
    val listrik: String = ""
) : Parcelable

@Parcelize
data class Lokasi(
    var jarak: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable