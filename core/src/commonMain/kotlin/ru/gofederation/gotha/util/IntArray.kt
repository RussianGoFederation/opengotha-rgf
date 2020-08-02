package ru.gofederation.gotha.util

/**
 * Creates new [IntArray] of size [size].
 *
 * The function [init] is called for each array element sequentially starting from the first one
 * and should return the value of array element.
 * First [init] argument is array index, second is previous element value or [first] for first element.
 */
fun intArrayWithPrevious(size: Int, first: Int = 0, init: (Int, Int) -> Int): IntArray {
    var previous = first
    return IntArray(size) { n ->
        previous = init(n, previous)
        previous
    }
}

/**
 * Returns sum of this [IntArray]'s two smallest elements or 0 if array's size is < 2
 */
fun IntArray.min2sum(): Int {
    if (this.size <= 2) return 0
    var iMin = 0
    for (i in indices) {
        if (this[i] < this[iMin]) iMin = i
    }
    var iMin2 = if (iMin == 0) 1 else 0
    for (i in indices) {
        if (i == iMin) continue
        if (this[i] < this[iMin2]) iMin2 = i
    }
    return this[iMin] + this[iMin2]
}
