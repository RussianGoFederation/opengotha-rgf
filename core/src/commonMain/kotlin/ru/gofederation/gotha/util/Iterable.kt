package ru.gofederation.gotha.util

inline fun <T> Iterable<T>.sumByIndexed(selector: (Int, T) -> Int): Int {
    var sum = 0
    this.forEachIndexed { index, element ->
        sum += selector(index, element)
    }
    return sum
}
