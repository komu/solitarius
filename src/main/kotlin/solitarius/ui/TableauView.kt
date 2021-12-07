package solitarius.ui

import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.*
import javax.swing.JComponent

import solitarius.general.*
import solitarius.ui.CardImages.backside
import solitarius.ui.CardImages.cardImages
import solitarius.ui.CardImages.cardHeight
import solitarius.ui.CardImages.cardWidth
import java.awt.Color

class TableauView(private val model: Tableau) : JComponent() {

    val marginTop = 5
    val marginRight = 5
    val marginBottom = 5
    val marginLeft = 5
    val preferredPadding = 5
    val cascadeDy = 20
    var currentDrag: Drag? = null

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

    override fun paint(g: Graphics) {
        paintTableau(g)

        val drag = currentDrag
        if (drag != null)
            drawCards(g, drag.x, drag.y, drag.sequence)
    }

    private fun paintTableau(g: Graphics) {
        for ((x, y, pile) in model.allPiles) {
            val (xx, yy) = gridCoordinates(x, y)
            drawPile(g, pile, xx, yy)
        }
    }

    private val horizontalPadding: Int
        get() {
            val contentWidth = width - marginLeft - marginRight
            val cardWidths = model.columnCount * cardWidth
            return (contentWidth - cardWidths) / (model.columnCount - 1)
        }

    private val verticalPadding: Int
        get() {
            val contentHeight = height - marginTop - marginTop
            val cardHeights = model.rowHeights.sum() * cardHeight
            return (contentHeight - cardHeights) / (model.rowCount - 1)
        }

    private fun gridCoordinates(x: Int, y: Int): Pair<Int, Int> {
        val prev = cardHeight * model.rowHeights.take(y).sum()

        return Pair(
            marginLeft + (cardWidth + horizontalPadding) * x,
            marginTop + prev + (verticalPadding * y)
        )
    }

    private fun drawPile(g: Graphics, pile: Pile, x: Int, y: Int) {
        val dragged = currentDrag?.takeIf { it.pile === pile }?.sequence?.size ?: 0
        val isEmpty = (pile.size - dragged) == 0

        if (isEmpty) {
            g.color = Color.GRAY
            g.fillRect(x, y, cardWidth, cardHeight)
        } else if (pile.showAsCascade) {
            drawCascaded(g, pile, x, y)
        } else {
            drawNonCascaded(g, pile, x, y)
        }
    }

    private fun drawCascaded(g: Graphics, pile: Pile, x: Int, startY: Int) {
        var y = startY

        repeat(pile.hiddenCount) {
            g.drawImage(backside, x, y, this)
            y += cascadeDy
        }

        val drag = currentDrag
        if (drag != null && drag.pile === pile) {
            drawCards(g, x, y, pile.visibleCards.drop(drag.sequence.size))
        } else {
            drawCards(g, x, y, pile.visibleCards)
        }
    }

    private fun drawNonCascaded(g: Graphics, pile: Pile, x: Int, y: Int) {
        val dragged = currentDrag?.takeIf { it.pile === pile }?.sequence?.size ?: 0
        val reallyVisible = pile.visibleCount - dragged

        if (reallyVisible > 0) {
            drawCard(g, pile.cards.drop(dragged).first(), x, y)
        } else {
            g.drawImage(backside, x, y, this)
        }
    }

    private fun drawCards(g: Graphics, x: Int, y: Int, cards: Collection<Card>) {
        var yy = y
        for (card in cards.reversed()) {
            drawCard(g, card, x, yy)
            yy += cascadeDy
        }
    }

    private fun drawCard(g: Graphics, card: Card, x: Int, y: Int) {
        g.drawImage(cardImages[card], x, y, this)
    }

    class Drag(
        val pile: Pile,
        val sequence: CardSequence,
        private val dx: Int,
        private val dy: Int,
        var mouseX: Int,
        var mouseY: Int
    ) {
        val x: Int
            get() = mouseX - dx
        val y: Int
            get() = mouseY - dy
    }

    private fun positionToCardLocation(x: Int, y: Int) =
        boundsForCards.find { it.first.inBounds(x, y) }?.let { p ->
            val (bounds, pile, cardIndex) = p
            val (xx, yy) = bounds.relative(x, y)
            val take = if (pile.showAsCascade) pile.size - cardIndex else 1
            CardLocation(pile, take, xx, yy)
        }

    private fun pile(x: Int, y: Int): Pile? =
        boundsForPiles.find { it.first.inBounds(x, y) }?.second

    private val boundsForCards: Collection<Triple<Bounds, Pile, Int>>
        get() = buildList {
            for ((bounds, pile) in boundsForPiles)
                for (result in boundsForCardsOfPile(bounds, pile))
                    add(result)
        }

    private fun boundsForCardsOfPile(bounds: Bounds, pile: Pile): List<Triple<Bounds, Pile, Int>> =
        if (pile.showAsCascade) {
            buildList {
                for (cardIndex in pile.size - 1 downTo 0) {
                    val top = bounds.y + (cardIndex * cascadeDy)
                    add(Triple(Bounds(bounds.x, top, cardWidth, cardHeight), pile, cardIndex))
                }
            }
        } else {
            listOf(Triple(bounds, pile, 0))
        }

    val boundsForPiles: Collection<Pair<Bounds, Pile>>
        get() = buildList {
            for ((x, y, pile) in model.allPiles) {
                val (xx, yy) = gridCoordinates(x, y)
                add(Pair(Bounds(xx, yy, cardWidth, pileHeight(pile)), pile))
            }
        }

    private fun pileHeight(pile: Pile) =
        if (pile.showAsCascade) cardHeight + (pile.size * cascadeDy) else cardHeight

    private class CardLocation(val pile: Pile, val cardCount: Int, val dx: Int, val dy: Int)

    private inner class MyMouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            positionToCardLocation(e.x, e.y)?.takeIf { it.cardCount == 1 }?.let { location ->
                model.pileClicked(location.pile, e.clickCount)
                repaint()
            }
        }

        override fun mousePressed(e: MouseEvent) {
            currentDrag = positionToCardLocation(e.x, e.y)?.let { location ->
                location.pile.sequence(location.cardCount)?.let { seq ->
                    Drag(location.pile, seq, location.dx, location.dy, e.x, e.y)
                }
            }
            repaint()
        }

        override fun mouseReleased(e: MouseEvent) {
            currentDrag?.let { drag ->
                pile(e.x, e.y)?.drop(drag.sequence)

                currentDrag = null
                repaint()
            }
        }
    }

    private inner class MyMouseMotionListener : MouseMotionAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            currentDrag?.let { drag ->
                drag.mouseX = e.x
                drag.mouseY = e.y
                repaint()
            }
        }
    }
}

class Bounds(val x: Int, val y: Int, val w: Int, val h: Int) {
    fun inBounds(xx: Int, yy: Int) =
        (xx >= x) && (xx < x + w) && (yy >= y) && (yy < y + h)

    fun relative(xx: Int, yy: Int) =
        Pair(xx - x, yy - y)
}
