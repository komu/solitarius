package solitarius.general

enum class CardColor {
    Red, Black;

    val opposing: CardColor
        get() = when (this) {
            Red -> Black
            Black -> Red
        }
}

enum class Suit(val label: String, val color: CardColor) {
    Heart("Heart", CardColor.Red),
    Spade("Spade", CardColor.Black),
    Diamond("Diamond", CardColor.Red),
    Club("Club", CardColor.Black);

    override fun toString() = label

    companion object {
        const val cardsInSuit = 13
        val suits = values().toList()
    }
}

enum class Rank(val shortName: String, val longName: String, val value: Int) {
    Ace("A", "Ace", 1),
    Deuce("2", "2", 2),
    Three("3", "3", 3),
    Four("4", "4", 4),
    Five("5", "5", 5),
    Six("6", "6", 6),
    Seven("7", "7", 7),
    Eight("8", "8", 8),
    Nine("9", "9", 9),
    Ten("10", "10", 10),
    Jack("J", "Jack", 11),
    Queen("Q", "Queen", 12),
    King("K", "King", 13);

    fun isPrevious(rank: Rank) =
        value == rank.value - 1

    override fun toString() = longName

    companion object {
        val ranks = values().toList()
    }
}

data class Card(val rank: Rank, val suit: Suit) {
    override fun toString() = "${rank.longName} of ${suit.label}"
}

object Deck {
    val cards = buildList { for (suit in Suit.suits) for (rank in Rank.ranks) add(Card(rank, suit)) }
    val hearts = ofSuit(Suit.Heart)
    val spades = ofSuit(Suit.Spade)
    val diamonds = ofSuit(Suit.Diamond)
    val clubs = ofSuit(Suit.Spade)

    fun ofSuit(suit: Suit) = Rank.ranks.map { Card(it, suit) }

    fun shuffledCards() = cards.shuffled()
    fun shuffledDecks(deckCount: Int) = decks(deckCount).shuffled()

    fun decks(count: Int): List<Card> = buildList {
        repeat(count) {
            addAll(cards)
        }
    }
}
