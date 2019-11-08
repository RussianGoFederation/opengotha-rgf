package ru.gofederation.gotha.ui.component

import kotlinx.coroutines.channels.Channel
import javax.swing.JProgressBar

suspend fun JProgressBar.watchProgress(progress: Channel<Pair<Long, Long>>) {
    isIndeterminate = true

    for (step in progress) {
        if (step.second > 0) {
            isIndeterminate = false
            maximum = step.second.toInt()
            value = step.first.toInt()
        } else {
            isIndeterminate = true
        }
    }

    isIndeterminate = false
    value = 100
    maximum = 100
}
