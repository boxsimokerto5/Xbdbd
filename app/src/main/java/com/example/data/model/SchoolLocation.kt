package com.example.data.model

data class SchoolLocation(
    val name: String = "Sekolah Rakyat Surabaya / Jakarta",
    val address: String = "Jl. Pendidikan Merdeka No. 10",
    val latitude: Double = -7.2575,
    val longitude: Double = 112.7521,
    val contactPhone: String = "0812-9988-7766"
)

data class PromoBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String = "INFO RESMI",
    val authorRole: String = "Admin Sekolah",
    val dateUpdated: String = "Hari ini"
)
