package ru.gofederation.gotha.model

fun egfPin(pin: String?): EgfPin? =
    if (null != pin && pin.isNotBlank()) {
        EgfPin(pin)
    } else {
        null
    }

class EgfPin(val pin: String)
