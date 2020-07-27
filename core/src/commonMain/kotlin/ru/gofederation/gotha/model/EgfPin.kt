package ru.gofederation.gotha.model

import ru.gofederation.gotha.util.Serializable

fun egfPin(pin: String?): EgfPin? =
    if (null != pin && pin.isNotBlank()) {
        EgfPin(pin)
    } else {
        null
    }

class EgfPin(val pin: String) : Serializable
