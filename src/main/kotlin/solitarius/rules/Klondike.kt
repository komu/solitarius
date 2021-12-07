package solitarius.rules

import solitarius.general.*

class KlondikePile : AlternateColorCascade() {
    override val longestDraggableSequence: Int
        get() = longestSequence { previous, card ->
            card.suit.color == previous.suit.color.opposing && previous.rank.isPrevious(card.rank)
        }

    override fun canDropOnEmpty(card: Card) = card.rank == Rank.King
}

class KlondikeTableau : Tableau(2, 7) {
    private val waste = Waste()
    private val stock = Stock(Deck.shuffledCards())
    private val foundations = List(4) { BySuitFoundation() }
    private val piles = List(7) { KlondikePile() }

    init {
        for (s in piles.indices)
            for (n in s until piles.size)
                piles[n].push(stock.takeOne()!!)

        for (pile in piles)
            pile.showOnlyTop()
    }

    override fun buildLayout() = buildList {
        add(stock)
        add(waste)
        add(empty)
        addAll(foundations)
        addAll(piles)
    }


    private fun deal() {
        val card = stock.takeOne()
        if (card != null)
            waste.push(card)
        else {
            stock.pushAll(waste.cards)
            waste.clear()
        }
    }

    override fun pileClicked(pile: Pile, count: Int) {
        if (pile == stock) {
            deal()
        } else if (count == 2) {
            val seq = pile.sequence(1)
            if (seq != null) {
                foundations.find { it.canDrop(seq) }?.drop(seq)
            }
        }
    }
}
