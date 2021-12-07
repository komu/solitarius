package solitarius.rules

import solitarius.general.*
import kotlin.test.*

class SpiderTest {

    @Test
    fun `pile should show only the topmost card`() {
        val pile = SpiderPile()
        val cards = Deck.shuffledCards().take(6)
        for (card in cards.asReversed())
            pile.push(card)
        pile.showOnlyTop()

        assertFalse(pile.isEmpty)
        assertEquals(6, pile.size)
        assertEquals(cards.first(), pile.top)
        assertEquals(cards.take(1), pile.visibleCards)
        assertEquals(1, pile.visibleCount)
    }

    @Test
    fun `pile should be allowed to be empty`() {
        val pile = SpiderPile()

        assertTrue(pile.isEmpty)
        assertEquals(0, pile.size)
        assertNull(pile.top)
        assertEquals(emptyList(), pile.visibleCards)
        assertEquals(0, pile.visibleCount)
    }

    @Test
    fun `pile should allow dropping cards on top`() {
        val pile = SpiderPile()

        val cards = listOf(Rank.Five, Rank.Six, Rank.Eight, Rank.Jack, Rank.Ace, Rank.King).map { Card(it, Suit.Heart) }
        val five = cards.first()
        val rest = cards.drop(1)

        for (c in rest.asReversed())
            pile.push(c)

        pile.showOnlyTop()

        val seq = DummySequence(five)

        assertTrue(pile.canDrop(seq))
        pile.drop(seq)

        assertFalse(pile.isEmpty)
        assertEquals(6, pile.size)
        assertEquals(five, pile.top)
        assertEquals(listOf(five, rest.first()), pile.visibleCards)
        assertEquals(2, pile.visibleCount)
    }

    @Test
    fun `flip the new top card to be visible when top card is popped`() {
        val pile = SpiderPile()
        val cards = Deck.shuffledCards().take(6)

        for (c in cards.asReversed())
            pile.push(c)
        pile.showOnlyTop()

        val seq = pile.sequence(1)
        assertNotNull(seq)
        assertEquals(cards.take(1), seq.toList())
        seq.removeFromOriginalPile()
        assertEquals(5, pile.size)
        assertEquals(cards[1], pile.top)
        assertEquals(listOf(cards[1]), pile.visibleCards)
        assertEquals(1, pile.visibleCount)
    }

    @Test
    fun `support popping the last card`() {
        val pile = SpiderPile()
        val cards = Deck.shuffledCards().take(1)

        pile.push(cards.first())

        assertEquals(cards, pile.visibleCards)
        assertEquals(1, pile.visibleCount)
        val seq = pile.sequence(1)
        assertNotNull(seq)
        assertEquals(cards, seq.toList())
        seq.removeFromOriginalPile()
        assertEquals(emptyList(), pile.visibleCards)
        assertEquals(0, pile.visibleCount)
    }

    @Test
    fun `support returning sequences of cards`() {
        val pile = SpiderPile()
        val cards = listOf(Rank.Five, Rank.Six, Rank.Seven, Rank.Nine, Rank.Jack, Rank.Ace, Rank.King).map {
            Card(it, Suit.Heart)
        }
        for (c in cards.drop(3).asReversed())
            pile.push(c)
        pile.showOnlyTop()

        for (c in cards.take(3).asReversed())
            pile.push(c)

        assertEquals(3, pile.longestDraggableSequence)
    }

    private class DummySequence(vararg cards: Card) : CardSequence(cards.asList()) {
        override fun removeFromOriginalPile() {}
    }
}
