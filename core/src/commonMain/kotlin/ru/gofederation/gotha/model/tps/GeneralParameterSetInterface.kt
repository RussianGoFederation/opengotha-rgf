package ru.gofederation.gotha.model.tps

// TODO: remove this interface once GeneralParameterSet is moved into core module
interface GeneralParameterSetInterface {
    val numberOfRounds: Int
    val numberOfCategories: Int
    val lowerCategoryLimits: IntArray
    val genMMZero: Int
    val genMMFloor: Int
    val genMMBar: Int
    val genMMS2ValueAbsent: Int
    val genMMS2ValueBye: Int
    val genNBW2ValueAbsent: Int
    val genNBW2ValueBye: Int
    val isGenRoundDownNBWMMS: Boolean
    val isGenCountNotPlayedGamesAsHalfPoint: Boolean
}
