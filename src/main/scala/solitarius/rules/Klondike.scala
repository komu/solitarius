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

class KlondikePile extends AlternateColorCascade {
  override def longestDraggableSequence = longestSequence { (previous, card) =>
    card.color == previous.color.opposing && card.value == previous.value + 1 
  }
  
  override def canDropOnEmpty(card: Card) = card.rank == Rank.King
}

class KlondikeTableau extends Tableau(2, 7) {
  val waste       = new Waste
  val stock       = new Stock(Deck.shuffledCards)
  val foundations = Array.fill(4)(new BySuitFoundation)
  val piles       = Array.fill(7)(new KlondikePile)

  for (s <- 0 until piles.size)
    for (n <- s until piles.size)
      piles(n).push(stock.takeOne().get)
  
  piles.foreach(_.showOnlyTop())

  def layout =
    stock :-: waste :-: empty :-: foundations :-:
    piles :-: end
  
  def deal() {
    stock.takeOne match {
      case Some(card) =>
        waste.push(card)
      case None =>
        stock.pushAll(waste.cards)
        waste.clear()
    }
  }
    
  override def pileClicked(pile: Pile, count: Int) {
    if (pile == stock) {
      deal()
    } else if (count == 2) {
      pile.sequence(1).foreach { seq =>
        foundations.find(_.canDrop(seq)).foreach { foundation =>
          foundation.drop(seq)
        }
      }
    }
  }
}
