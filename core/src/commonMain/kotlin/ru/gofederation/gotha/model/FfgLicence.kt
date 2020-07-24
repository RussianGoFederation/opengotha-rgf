package ru.gofederation.gotha.model

fun ffgLicence(licence: String?, status: String?): FfgLicence? =
    if (licence != null && status != null && licence.isNotBlank()) {
        FfgLicence(licence, status)
    } else {
        null
    }

class FfgLicence(
    val licence: String,
    val status: String
)
