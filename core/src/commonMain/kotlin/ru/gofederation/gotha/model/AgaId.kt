package ru.gofederation.gotha.model

fun agaId(id: String?, expirationDate: String?): AgaId? =
    if (null != id && null != expirationDate && id.isNotBlank()) {
        AgaId(id, expirationDate)
    } else {
        null
    }

class AgaId(
    val id: String,
    val expirationDate: String
)
