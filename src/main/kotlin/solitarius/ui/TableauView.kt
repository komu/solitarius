@file:OptIn(ExperimentalComposeUiApi::class)

package solitarius.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.*

import solitarius.general.*
import solitarius.ui.CardImages.backside
import solitarius.ui.CardImages.cardImages

private const val marginTop = 5
private const val marginRight = 5
private const val marginBottom = 5
private const val marginLeft = 5
private const val preferredPadding = 5

private val cascadeDy = 20.dp
private val cardSize = DpSize(71.dp, 96.dp)

@Composable
fun TableauView(model: Tableau) {
    var currentDrag by remember { mutableStateOf<Drag?>(null) }
    var paints by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()
        .onPointerEvent(PointerEventType.Press) {
            val change = it.changes.lastOrNull()

            if (change != null) {
                val dimensions = Dimensions(model, size.toSize(), this)
                currentDrag = dimensions.positionToCardLocation(change.position)?.let { location ->
                    location.pile.sequence(location.cardCount)?.let { seq ->
                        Drag(location.pile, seq, location.delta, change.position)
                    }
                }
            }
        }
        .onPointerEvent(PointerEventType.Release) {
            val change = it.changes.lastOrNull()

            if (change != null) {
                val dimensions = Dimensions(model, size.toSize(), this)

                val drag = currentDrag
                if (drag != null) {
                    dimensions.pile(change.position)?.drop(drag.sequence)
                } else {
                    val pile = dimensions.pile(change.position)
                    if (pile != null) {
                        model.pileClicked(pile, 1) // TODO: support double clicks
                        paints++
                    }
                }

                currentDrag = null
            }
        }
        .onPointerEvent(PointerEventType.Move) {
            for (change in it.changes) {
                currentDrag?.let { drag ->
                    currentDrag = drag.copy(mousePosition = change.position)
                }
            }
        }
        .drawBehind {
            TableauDrawer(this, model, paints).drawTableau(this, currentDrag)
        })
}

private class CardLocation(val pile: Pile, val cardCount: Int, val delta: Offset)

private class Bounds(val x: Float, val y: Float, private val size: Size) {
    fun inBounds(p: Offset) =
        (p.x >= x) && (p.x < x + size.width) && (p.y >= y) && (p.y < y + size.height)

    fun relative(p: Offset) =
        Pair(p.x - x, p.y - y)
}

private class Dimensions(
    private val model: Tableau,
    private val size: Size,
    private val density: Density
) {

    fun pile(position: Offset): Pile? =
        with(density) {
            boundsForPiles.find { it.first.inBounds(position) }?.second
        }

    fun gridCoordinates(x: Int, y: Int): Offset {
        with (density) {
            val prev = cardSize.height * model.rowHeights.take(y).sum()

            return Offset(
                x = marginLeft + (cardSize.width.toPx() + horizontalPadding) * x,
                y = marginTop + prev.toPx() + (verticalPadding * y)
            )
        }
    }

    fun positionToCardLocation(position: Offset) =
        with(density) {
            boundsForCards.find { it.first.inBounds(position) }?.let { p ->
                val (bounds, pile, cardIndex) = p
                val (xx, yy) = bounds.relative(position)
                val take = if (pile.showAsCascade) pile.size - cardIndex else 1
                CardLocation(pile, take, Offset(xx, yy))
            }
        }

    private val Density.horizontalPadding: Float
        get() {
            val contentWidth = size.width - marginLeft - marginRight
            val cardWidths = model.columnCount * cardSize.width
            return (contentWidth - cardWidths.toPx()) / (model.columnCount - 1)
        }

    private val Density.verticalPadding: Float
        get() {
            val contentHeight = size.height - marginTop - marginBottom
            val cardHeights = model.rowHeights.sum() * cardSize.height
            return (contentHeight - cardHeights.toPx()) / (model.rowCount - 1)
        }

    private val Density.boundsForCards: Collection<Triple<Bounds, Pile, Int>>
        get() = buildList {
            for ((bounds, pile) in boundsForPiles)
                for (result in boundsForCardsOfPile(bounds, pile))
                    add(result)
        }

    private val Density.boundsForPiles: Collection<Pair<Bounds, Pile>>
        get() = buildList {
            for ((x, y, pile) in model.allPiles) {
                val (xx, yy) = gridCoordinates(x, y)
                add(Pair(Bounds(xx, yy, DpSize(cardSize.width, pileHeight(pile)).toSize()), pile))
            }
        }

    private fun pileHeight(pile: Pile): Dp =
        if (pile.showAsCascade) cardSize.height + (pile.size * cascadeDy) else cardSize.height

    private fun Density.boundsForCardsOfPile(bounds: Bounds, pile: Pile): List<Triple<Bounds, Pile, Int>> =
        if (pile.showAsCascade) {
            buildList {
                for (cardIndex in pile.size - 1 downTo 0) {
                    val top = bounds.y + (cardIndex * cascadeDy.toPx())
                    add(Triple(Bounds(bounds.x, top, cardSize.toSize()), pile, cardIndex))
                }
            }
        } else {
            listOf(Triple(bounds, pile, 0))
        }
}

private class TableauDrawer(
    private val drawScope: DrawScope,
    private val model: Tableau,
    paint: Int
) {

    private val dimensions = Dimensions(model, drawScope.size, drawScope)

    fun drawTableau(drawScope: DrawScope, currentDrag: Drag?) {
        with(drawScope) {
            for ((x, y, pile) in model.allPiles) {
                val offset = dimensions.gridCoordinates(x, y)
                drawPile(pile, offset, currentDrag)
            }

            if (currentDrag != null)
                drawCards(currentDrag.offset, currentDrag.sequence)
        }
    }

    private fun DrawScope.drawPile(pile: Pile, offset: Offset, currentDrag: Drag?) {
        val dragged = currentDrag?.takeIf { it.pile === pile }?.sequence?.size ?: 0
        val isEmpty = (pile.size - dragged) == 0

        when {
            isEmpty ->
                drawScope.drawRect(Color.Gray, topLeft = offset, size = cardSize.toSize())
            pile.showAsCascade ->
                drawCascaded(pile, offset, currentDrag)
            else -> {
                drawNonCascaded(pile, offset, currentDrag)
            }
        }
    }

    private fun DrawScope.drawCascaded(pile: Pile, topLeft: Offset, currentDrag: Drag?) {
        var offset = topLeft

        repeat(pile.hiddenCount) {
            drawCardImage(backside, offset)
            offset = offset.copy(y = offset.y + cascadeDy.toPx())
        }

        if (currentDrag != null && currentDrag.pile === pile) {
            drawCards(offset, pile.visibleCards.drop(currentDrag.sequence.size))
        } else {
            drawCards(offset, pile.visibleCards)
        }
    }

    private fun DrawScope.drawNonCascaded(pile: Pile, offset: Offset, currentDrag: Drag?) {
        val dragged = currentDrag?.takeIf { it.pile === pile }?.sequence?.size ?: 0
        val reallyVisible = pile.visibleCount - dragged

        if (reallyVisible > 0) {
            drawCard(pile.cards.drop(dragged).first(), offset)
        } else {
            drawCardImage(backside, offset)
        }
    }

    private fun DrawScope.drawCards(offset: Offset, cards: Collection<Card>) {
        var y = offset.y
        for (card in cards.reversed()) {
            drawCard(card, offset.copy(y = y))
            y += cascadeDy.toPx()
        }
    }

    private fun DrawScope.drawCard(card: Card, offset: Offset) {
        drawCardImage(cardImages[card]!!, offset)
    }

    private fun DrawScope.drawCardImage(image: ImageBitmap, topLeft: Offset) {
        drawImage(image, dstOffset = topLeft.toIntOffset(), dstSize = cardSize.toSize().toIntSize())
    }


/*
    init {
        addMouseListener(MyMouseListener())
        addMouseMotionListener(MyMouseMotionListener())
        preferredSize = Dimension(
            marginLeft + marginRight + model.columnCount * (preferredPadding + cardWidth),
            marginTop + marginBottom + preferredRowHeights.sum()
        )
        minimumSize = preferredSize
    }

    private val preferredRowHeights: List<Int>
        get() = model.rowHeights.map { cards -> preferredPadding + cards * cardHeight }


    */
}

private data class Drag(
    val pile: Pile,
    val sequence: CardSequence,
    private val delta: Offset,
    var mousePosition: Offset,
) {

    val offset: Offset
        get() = mousePosition - delta
}
