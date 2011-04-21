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
package solitarius.rules

import org.specs._
import solitarius.general._
import Utils.shuffled
import Rank._

object SpiderSpecification extends Specification {
  "Pile" should {
    "show only the topmost card" in {
      val pile = new SpiderPile
      val (card :: cards) = randomCards(6)
      cards.reverse.foreach(pile.push)
      pile.showOnlyTop()

      pile.isEmpty      must beFalse
      pile.size         mustEqual 5
      pile.top          mustEqual Some(cards.head)
      pile.visibleCards mustEqual List(cards.head)
      pile.visibleCount mustEqual 1
    }
    
    "be allowed to be empty" in {
      val pile = new SpiderPile
            
      pile.isEmpty      must beTrue
      pile.size         mustEqual 0
      pile.top          mustEqual None
      pile.visibleCards mustEqual Nil
      pile.visibleCount mustEqual 0
    }
    
    "allow dropping cards on top" in {
      val pile = new SpiderPile
      val (card :: cards) = hearts(Five, Six, Eight, Jack, Ace, King)
      cards.reverse.foreach(pile.push)
      pile.showOnlyTop()

      val seq = new DummySequence(card)

      pile.canDrop(seq) must beTrue
      pile.drop(seq)

      pile.isEmpty      must beFalse
      pile.size         mustEqual 6
      pile.top          mustEqual Some(card)
      pile.visibleCards mustEqual List(card, cards.head)
      pile.visibleCount mustEqual 2
    }
    
    "flip the new top card to be visible when top card is popped" in {
      val pile = new SpiderPile
      val cards = randomCards(6)
      cards.reverse.foreach(pile.push)
      pile.showOnlyTop()
      
      val seq = pile.sequence(1)
      seq                 must beSome[Sequence]
      seq.get.toList      mustEqual List(cards(0))
      seq.get.removeFromOriginalPile()
      pile.size           mustEqual 5
      pile.top            mustEqual Some(cards(1))
      pile.visibleCards   mustEqual List(cards(1))
      pile.visibleCount   mustEqual 1
    }
    
    "support popping the last card" in {
      val pile = new SpiderPile
      val cards = randomCards(1)
      pile.push(cards.head)
      
      pile.visibleCards   mustEqual cards
      pile.visibleCount   mustEqual 1
      val seq = pile.sequence(1)
      seq                 must beSome[Sequence]
      seq.get.toList      mustEqual List(cards.head)
      seq.get.removeFromOriginalPile
      pile.visibleCards   must beEmpty
      pile.visibleCount   mustEqual 0
    }
    
    "support returning sequences of cards" in {
      val pile = new SpiderPile
      val cards = hearts(Five, Six, Seven, Nine, Jack, Ace, King)
      cards.drop(3).reverse.foreach(pile.push)
      pile.showOnlyTop()
      cards.take(3).reverse.foreach(pile.push)
      
      pile.longestDraggableSequence mustEqual 3
    }
  }
  
  def hearts(ranks: Rank*) = ranks.map(new Card(_, Suit.Heart)).toList
  
  def randomCards(count: Int) = Deck.shuffledCards.take(count)

  class DummySequence(cards: Card*) extends Sequence(cards.toList) {
    override def removeFromOriginalPile() { }
  }
}
