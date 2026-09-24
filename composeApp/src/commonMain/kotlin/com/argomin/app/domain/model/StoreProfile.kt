package com.argomin.app.domain.model

data class StoreProfile(
    val name: String = "Rumah Makan Padang tambooPOS",
    val address: String = "Jl. Contoh Raya No. 123, Padang",
    val phone: String = "+62 812-3456-7890",
    val taxId: String = "01.234.567.8-901.000",
    val qrisMerchantName: String = "RM ARGO MINANG 3 KEPEK, P",
    val qrisNmid: String = "ID1026469990109",
    val currency: String = "Rupiah (IDR)"
)
