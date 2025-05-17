package com.myskripsi.gokos.data.model

data class Campus (
    val id: String = "",
    val campusName: String = "",
    val address: String = "",
    val logoUrl: String = "",
    val location: Location = Location(),
    val radius: Int = 0
)

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
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Lokasi(
    val jarak: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)