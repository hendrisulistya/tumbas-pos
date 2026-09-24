package com.argomin.app.domain.model

data class StoreProfile(
    val name: String = "Rumah Makan Padang tambooPOS",
    val address: String = "Jl. Contoh Raya No. 123, Padang",
    val phone: String = "+62 812-3456-7890",
    val taxId: String = "01.234.567.8-901.000",
    val qrisMerchantName: String = "RM ARGO MINANG 3 KEPEK, P",
    val qrisNmid: String = "ID1026469990109",
    val qrisPayload: String = "00020101021126610014COM.GO-JEK.WWW01189360091433961813320210G3961813320303UMI51440014ID.CO.QRIS.WWW0215ID10264699901090303UMI5204581253033605802ID5925RM ARGO MINANG 3 KEPEK, P6011KULON PROGO61055565262070703A0263041DE8",
    val qrisCity: String = "KULON PROGO",
    val isQrisVerified: Boolean = true,
    val currency: String = "Rupiah (IDR)"
)
