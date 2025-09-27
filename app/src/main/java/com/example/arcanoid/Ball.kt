package com.example.arcanoid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

class Ball(
    position: Offset,
    velocity: Offset
) {
    var position by mutableStateOf(position)
    var velocity by mutableStateOf(velocity)
}