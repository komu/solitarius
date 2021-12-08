package solitarius.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize


fun IntSize.toSize() = Size(width.toFloat(), height.toFloat())
fun Size.toIntSize() = IntSize(width.toInt(), height.toInt())

fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())
