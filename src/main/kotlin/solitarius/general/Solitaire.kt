/*
 *  Copyright 2008-2011 Juha Komulainen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package solitarius.general

abstract class Pile {
    abstract val showAsCascade: Boolean
    abstract val cards: List<Card>
    abstract val visibleCount: Int

    val top: Card?
        get() = cards.firstOrNull()

    val size: Int
        get() = cards.size

    val hiddenCount: Int
        get() = size - visibleCount

    val isEmpty: Boolean
        get() = size == 0

    val visibleCards: List<Card>
        get() = cards.take(visibleCount)

    val hiddenCards: List<Card>
        get() = cards.drop(visibleCount)

    open fun sequence(count: Int): CardSequence? = null

    open fun drop(sequence: CardSequence): Boolean = false

    companion object {
        fun dealToPiles(piles: List<BasicPile>, cards: List<Card>) {
            for ((i, card) in cards.withIndex())
                piles[i % piles.size].push(card)
        }
    }
}

/**
 * Pile is a possibly empty stack of cards on Tableau. If the pile is not
 * empty, then one or more cards on top of the pile are face up and therefore
 * visible.
 */
abstract class BasicPile : Pile() {

    private val _cards = mutableListOf<Card>()
    private var _visibleCount = 0

    override val cards: List<Card>
        get() = _cards

    override val visibleCount: Int
        get() = _visibleCount

    override val showAsCascade = true

    abstract val longestDraggableSequence: Int

    abstract fun canDrop(sequence: CardSequence): Boolean
    protected open fun afterModification() {
    }

    override fun sequence(count: Int): CardSequence? =
        if (count <= longestDraggableSequence)
            BasicCardSequence(cards.take(count))
        else
            null

    override fun drop(sequence: CardSequence): Boolean =
        if (canDrop(sequence)) {
            sequence.removeFromOriginalPile()

            _cards.addAll(0, sequence)
            _visibleCount += sequence.size

            afterModification()
            true
        } else {
            false
        }

    private fun pop(): Card? {
        val card = _cards.removeFirstOrNull()
        if (card != null) {
            afterModification()
            _visibleCount = if (_cards.isEmpty()) 0 else maxOf(_visibleCount - 1, 1)
        }
        return card
    }

    fun clear() {
        _cards.clear()
        _visibleCount = 0
    }

    inner class BasicCardSequence(cards: List<Card>) : CardSequence(cards) {
        override fun removeFromOriginalPile() {
            repeat(cards.size) {
                pop()
            }
        }
    }

    fun showOnlyTop() {
        _visibleCount = if (cards.isEmpty()) 0 else 1
    }

    fun push(card: Card) {
        _cards.add(0, card)
        _visibleCount += 1
    }

    protected fun longestSequence(predicate: (Card, Card) -> Boolean): Int {
        val visible = visibleCards

        if (visible.isEmpty())
            return 0

        var count = 1

        var previous = visible.first()
        for (card in visible.drop(1)) {
            if (!predicate(previous, card)) {
                break
            }

            count += 1
            previous = card
        }

        return count
    }
}

/**
 * Base class for cascades that are built down by cards of alternating colors.
 */
abstract class AlternateColorCascade : BasicPile() {
    override fun canDrop(sequence: CardSequence): Boolean {
        val card = top
        val bottomCard = sequence.bottomCard

        return if (card != null)
            bottomCard.suit.color == card.suit.color.opposing && bottomCard.rank.isPrevious(card.rank)
        else
            canDropOnEmpty(bottomCard)
    }

    abstract fun canDropOnEmpty(card: Card): Boolean
}

/**
 * General base class for foundations.
 */
abstract class Foundation : BasicPile() {
    override val showAsCascade = false
    override val longestDraggableSequence = 0
}

/**
 * A foundation that is built up by suited cards from Ace to King.
 */
class BySuitFoundation : Foundation() {
    override fun canDrop(sequence: CardSequence) = canDrop(sequence.bottomCard)

    private fun canDrop(card: Card): Boolean {
        val top = top
        return if (top != null) {
            card.suit == top.suit && top.rank.isPrevious(card.rank)
        } else {
            card.rank == Rank.Ace
        }
    }
}

/**
 * Waste piles are piles that are not shown as cascade and where we can't
 * drop anything manually, but we can drag cards away from them.
 */
class Waste : BasicPile() {
    override val showAsCascade = false
    override val longestDraggableSequence: Int
        get() = if (isEmpty) 0 else 1

    override fun canDrop(sequence: CardSequence) = false
}


/**
 * Cells are piles that can contain only zero or one cards.
 */
class Cell : Pile() {
    var card: Card? = null

    override val cards: List<Card>
        get() = listOfNotNull(card)
    override val visibleCount: Int
        get() = size
    override val showAsCascade = false

    override fun sequence(count: Int): CardSequence? =
        if (count == 1)
            card?.let(::CellSequence)
        else
            null

    override fun drop(sequence: CardSequence) =
        if (isEmpty && sequence.size == 1) {
            sequence.removeFromOriginalPile()

            card = sequence.bottomCard
            true
        } else {
            false
        }

    private inner class CellSequence(card: Card) : CardSequence(listOf(card)) {
        override fun removeFromOriginalPile() {
            card = null
        }
    }
}

/**
 * CardSequence is a non-empty list of cards that can be moved together.
 * The exact rules of what forms a valid sequence depends on the game.
 */
abstract class CardSequence(val cards: List<Card>) : AbstractList<Card>() {

    init {
        require(cards.isNotEmpty()) { "empty sequence" }
    }

    val bottomCard: Card
        get() = cards.last()

    override fun iterator() = cards.iterator()
    override val size: Int = cards.size
    override fun get(index: Int) = cards[index]

    val isSuited: Boolean
        get() = cards.all { it.suit == cards.first().suit }

    abstract fun removeFromOriginalPile()
}

/**
 * Stock contains cards that are not dealt in the beginning.
 */
class Stock(cards: List<Card>) : Pile() {

    private val _cards = cards.toMutableList()
    override val showAsCascade = false
    override val cards: List<Card>
        get() = _cards

    override val visibleCount = 0

    fun takeOne() = take(1).firstOrNull()

    fun take(count: Int): List<Card> {
        if (_cards.isEmpty())
            return emptyList()

        val taken = _cards.take(count)
        _cards.subList(0, count).clear()
        return taken
    }

    fun pushAll(newCards: List<Card>) {
        _cards.addAll(0, newCards.asReversed())
    }
}
