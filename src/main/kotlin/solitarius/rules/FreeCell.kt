package solitarius.rules

import solitarius.general.*
import solitarius.general.Pile.Companion.dealToPiles

class FreeCellPile : AlternateColorCascade() {
    override val longestDraggableSequence: Int
        get() = if (isEmpty) 0 else 1

    override fun canDropOnEmpty(card: Card) = true
}

class FreeCellTableau : Tableau(2, 8) {
    private val cells = List(4) { Cell() }
    private val foundations = List(4) { BySuitFoundation() }
    private val piles = List(8) { FreeCellPile() }

    init {
        dealToPiles(piles, Deck.shuffledCards())
    }

    override fun buildLayout() = buildList {
        addAll(cells)
        addAll(foundations)
        addAll(piles)
    }
}
