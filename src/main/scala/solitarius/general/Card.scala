/*
 *  Copyright 2008 Juha Komulainen
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

import Utils.shuffled

sealed abstract class CardColor {
  def opposing: CardColor
}

object CardColor {
  object Black extends CardColor {
    def opposing = Red
  }
  object Red extends CardColor {
    def opposing = Black
  }
}

sealed abstract class Suit(val name: String, val color: CardColor) {
  override def toString = name
}

object Suit {
  case object Heart   extends Suit("Heart",   CardColor.Red)
  case object Spade   extends Suit("Spade",   CardColor.Black)
  case object Diamond extends Suit("Diamond", CardColor.Red)
  case object Club    extends Suit("Club",    CardColor.Black)
  
  val cardsInSuit = 13
  val suits = List(Heart, Spade, Diamond, Club)
}

sealed abstract class Rank(val shortName: String, val longName: String, val value: Int) {
  override def toString = longName
}

object Rank {
  case object Ace   extends Rank("A", "Ace",  1)
  case object Deuce extends Rank("2",   "2",  2)
  case object Three extends Rank("3",   "3",  3)
  case object Four  extends Rank("4",   "4",  4)
  case object Five  extends Rank("5",   "5",  5)
  case object Six   extends Rank("6",   "6",  6)
  case object Seven extends Rank("7",   "7",  7)
  case object Eight extends Rank("8",   "8",  8)
  case object Nine  extends Rank("9",   "9",  9)
  case object Ten   extends Rank("10", "10",  10)
  case object Jack  extends Rank("J",  "Jack",  11)
  case object Queen extends Rank("Q",  "Queen", 12)
  case object King  extends Rank("K",  "King",  13)
  
  val ranks = List(Ace, Deuce, Three, Four, Five, Six, Seven, Eight, Nine,
                  Ten, Jack, Queen, King)
}

case class Card(rank: Rank, suit: Suit) {
  def value = rank.value
  def color = suit.color
  override def toString = rank.longName + " of " + suit.name
}

object Deck {
  val cards    = for (suit <- Suit.suits; rank <- Rank.ranks) yield Card(rank, suit)
  val hearts   = ofSuit(Suit.Heart)
  val spades   = ofSuit(Suit.Spade)
  val diamonds = ofSuit(Suit.Diamond)
  val clubs    = ofSuit(Suit.Spade)
  
  def ofSuit(suit: Suit): List[Card] = Rank.ranks.map(Card(_,suit))

  def shuffledCards = shuffled(cards).toList
  def shuffledDecks(deckCount: Int) = shuffled(decks(deckCount)).toList
  
  def decks(count: Int): List[Card] = if (count == 0) Nil else cards ::: decks(count - 1)
}
