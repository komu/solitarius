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
package solitarius.rules

import solitarius.general._
import Pile.dealToPiles
import Utils.{ replicate, shuffled }

sealed abstract class SpiderLevel(cards: List[Card]) {
  def newCards = shuffled(cards).toList
}

object SpiderLevel {
  case object Easy   extends SpiderLevel(replicate(8, Deck.hearts))
  case object Medium extends SpiderLevel(replicate(4, Deck.hearts ::: Deck.spades))
  case object Hard   extends SpiderLevel(replicate(2, Deck.cards))
}

class SpiderPile extends BasicPile {
  
  override def canDrop(sequence: Sequence) = top match {
    case Some(c) => sequence.bottomRank.value == c.value - 1
    case None    => true
  }
  
  override def afterModification() {
    sequence(Suit.cardsInSuit).filter(_.isSuited).foreach { seq =>
      seq.removeFromOriginalPile()
    }
  }
  
  override def longestDraggableSequence = longestSequence { (previous, card) =>
    card.suit == previous.suit && card.value == previous.value + 1 
  }
}

class SpiderTableau(level: SpiderLevel) extends Tableau(2, 10) {
  val cards = level.newCards
  val piles = Array.fill(10)(new SpiderPile)
  val reserve = new Stock(cards.drop(54).toList)
  
  dealToPiles(piles, cards.take(54))
  piles.foreach(_.showOnlyTop)
  
  def layout = piles :-: reserve :-: end

  def hasEmptyPiles = piles.exists(_.isEmpty)
  
  def deal() {
    if (!hasEmptyPiles)
      for ((card, index) <- reserve.take(piles.size).zipWithIndex)
        piles(index).push(card)
  }
  
  override def pileClicked(pile: Pile, count: Int) {
    if (pile == reserve)
      deal()
  }
}
