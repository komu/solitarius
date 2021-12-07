package solitarius.rules

import solitarius.general.*
import solitarius.general.Pile.Companion.dealToPiles
import java.util.Collections.nCopies

enum class SpiderLevel(private val cards: List<Card>) {
    Easy(nCopies(8, Deck.hearts).flatten()),
    Medium(nCopies(4, Deck.hearts).flatten() + nCopies(4, Deck.spades).flatten()),
    Hard(nCopies(2, Deck.cards).flatten());

    fun newCards() = cards.shuffled()
}

class SpiderPile : BasicPile() {

    override fun canDrop(sequence: CardSequence): Boolean {
        val top = top
        return top == null || sequence.bottomCard.rank.isPrevious(top.rank)
    }

    override fun afterModification() {
        sequence(Suit.cardsInSuit)?.takeIf { it.isSuited }?.removeFromOriginalPile()
    }

    override val longestDraggableSequence: Int
        get() = longestSequence { previous, card ->
            card.suit == previous.suit && previous.rank.isPrevious(card.rank)
        }
}

class SpiderTableau(level: SpiderLevel) : Tableau(2, 10) {
    private val cards = level.newCards()
    private val piles = List(10) { SpiderPile() }
    private val reserve = Stock(cards.drop(54))

    init {
        dealToPiles(piles, cards.take(54))

        for (pile in piles)
            pile.showOnlyTop()
    }

    override fun buildLayout() = buildList {
        addAll(piles)
        add(reserve)
    }

    private val hasEmptyPiles: Boolean
        get() = piles.any { it.isEmpty }

    private fun deal() {
        if (!hasEmptyPiles)
            for ((index, card) in reserve.take(piles.size).withIndex())
                piles[index].push(card)
    }

    override fun pileClicked(pile: Pile, count: Int) {
        if (pile == reserve)
            deal()
    }
}
