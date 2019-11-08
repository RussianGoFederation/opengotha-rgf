package ru.gofederation.gotha.ui.component

import javax.swing.AbstractButton
import javax.swing.ButtonGroup

fun ButtonGroup.addAll(vararg buttons: AbstractButton) {
    for (button in buttons) {
        add(button)
    }
}
