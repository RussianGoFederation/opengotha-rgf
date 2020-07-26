package ru.gofederation.gotha

object Limits {
    // Should definitely stay below 16000, due to internal limits in PairingParameterSet parameter values
    // Should definitely stay below 9999, due to printing issues
    const val MAX_NUMBER_OF_PLAYERS = 1200
    const val MAX_NUMBER_OF_BOARDS = MAX_NUMBER_OF_PLAYERS / 2
    // Should definitely stay below or equal to 32, due to internal limits in costValue() method
    const val MAX_NUMBER_OF_ROUNDS = 20
}
