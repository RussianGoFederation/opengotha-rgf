package ru.gofederation.gotha.model.tps

// TODO: remove this interface once GeneralParameterSet is moved into core module
interface GeneralParameterSetInterface {
    val numberOfRounds: Int
    val numberOfCategories: Int
    val lowerCategoryLimits: IntArray
    val genMMZero: Int
    val genMMFloor: Int
    val genMMBar: Int
}
