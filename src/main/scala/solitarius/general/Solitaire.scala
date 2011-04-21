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
  val showAsCascade: Boolean
  def top = cards.headOption
  def size = cards.size
  def visibleCount: Int
  def hiddenCount  = size - visibleCount
  def isEmpty = size == 0
  def cards: List[Card]
  def visibleCards = cards.take(visibleCount)
  def hiddenCards  = cards.drop(visibleCount)
  def sequence(count: Int): Option[Sequence] = None
  def drop(sequence: Sequence): Boolean = false
}

object Pile {
  def dealToPiles(piles: Seq[BasicPile], cards: List[Card]) =
    for ((card, index) <- cards.zipWithIndex)
      piles(index % piles.size).push(card)
}

/**
 * Pile is a possibly empty stack of cards on Tableau. If the pile is not
 * empty, then one or more cards on top of the pile are face up and therefore
 * visible.
 */
abstract class BasicPile extends Pile {
  private var _cards: List[Card] = Nil
  private var _visible = 0
  
  override val showAsCascade = true
  override def isEmpty       = cards.isEmpty
  override def size          = cards.size
  override def visibleCount  = _visible
  override def cards         = _cards
  
  override def sequence(count: Int): Option[Sequence] = 
    if (count <= longestDraggableSequence)
      Some(new BasicSequence(cards.take(count)))
    else 
      None
      
  private class BasicSequence(cards: List[Card]) extends Sequence(cards) {
    override def removeFromOriginalPile() {
      for (_ <- 1 to cards.size)
        pop()
    }
  }
  
  private def pop(): Option[Card] = cards match {
    case (c :: cs) =>
      _visible = if (cs.isEmpty) 0 else (_visible - 1) max 1
      _cards = cs
      afterModification()
      Some(c)
    case Nil =>
      None
  }
  
  def clear() {
    _cards = Nil
    _visible = 0
  }
      
  override def drop(sequence: Sequence) = {
    if (canDrop(sequence)) {
      sequence.removeFromOriginalPile()
    
      _cards = sequence.toList ::: _cards
      _visible += sequence.size
    
      afterModification()
      true
    } else {
      false
    }
  }
  
  protected def canDrop(sequence: Sequence): Boolean
  protected def afterModification() { }
  
  def longestDraggableSequence: Int
  
  def showOnlyTop() { 
    _visible = if (cards.isEmpty) 0 else 1
  }
  
  def push(card: Card) {
    _cards ::= card
    _visible += 1
  }
  
  protected def longestSequence(predicate: (Card,Card) => Boolean): Int = 
    visibleCards match {
      case (first :: rest) =>
        var previous = first
        var count = 1
        for (card <- rest) {
          if (predicate(previous, card)) {
            count += 1
            previous = card
          } else {
            return count
          }
        }
        return count
      case Nil =>
        return 0
    }
}

/**
 * Base class for cascades that are built down by cards of alternating colors. 
 */
abstract class AlternateColorCascade extends BasicPile {
  override def canDrop(seq: Sequence) = top match {
    case Some(c) => 
      seq.bottomSuit.color == c.color.opposing && seq.bottomRank.value == c.value - 1
    case None => 
      canDropOnEmpty(seq.bottomCard)
  }
  
  def canDropOnEmpty(card: Card): Boolean
}

/**
 * General base class for foundations.
 */
abstract class Foundation extends BasicPile {
  override val showAsCascade = false
  override def longestDraggableSequence = 0
}

/**
 * A foundation that is built up by suited cards from Ace to King.
 */
class BySuitFoundation extends Foundation {
  override def canDrop(seq: Sequence) = canDrop(seq.bottomCard) 

  def canDrop(card: Card) = top match {
    case Some(top) => card.suit == top.suit && card.value == top.value + 1
    case None      => card.rank == Rank.Ace
  }
}

/**
 * Waste piles are piles that are not shown as cascade and where we can't
 * drop anything manually, but we can drag cards away from them.
 */
class Waste extends BasicPile {
  override val showAsCascade = false
  override def longestDraggableSequence = if (isEmpty) 0 else 1
  override def canDrop(sequence: Sequence) = false
}

/**
 * Cells are piles that can contain only zero or one cards.
 */
class Cell extends Pile {
  var card: Option[Card] = None
  
  override def cards = card.toList
  override def visibleCount = size
  override val showAsCascade = false

  override def sequence(count: Int) = 
    if (count == 1)
      card.map(new CellSequence(_)) 
    else 
      None

  override def drop(sequence: Sequence) =
    if (isEmpty && sequence.size == 1) {
      sequence.removeFromOriginalPile()
    
      card = Some(sequence.bottomCard)
      true
    } else {
      false
    }
  
  private class CellSequence(c: Card) extends Sequence(List(c)) {
    override def removeFromOriginalPile() {
      card = None
    }
  }
}

/**
 * Sequence is a non-empty list of cards that can be moved together.
 * The exact rules of what forms a valid sequence depends on the game.
 */
abstract class Sequence(cards: List[Card]) extends Seq[Card] {
  assert(!cards.isEmpty, "empty sequence")

  override def iterator = cards.iterator
  override def length = cards.length
  override def apply(i: Int) = cards.apply(i)
  override def toList = cards
  def bottomCard = cards.last
  def bottomSuit = bottomCard.suit
  def bottomRank = bottomCard.rank
  def isSuited   = cards.forall(_.suit == cards.head.suit)
  def removeFromOriginalPile(): Unit
}

/** 
 * Stock contains cards that are not dealt in the beginning.
 */
class Stock(private var _cards: List[Card]) extends Pile {
  override val showAsCascade = false
  override def cards = _cards
  override def visibleCount = 0
  override def isEmpty = cards.isEmpty
  override def size    = cards.size
  
  def takeOne() = take(1).headOption
  
  def take(count: Int) = {
    val (taken, rest) = cards.splitAt(count)
    _cards = rest
    taken
  }
  
  def pushAll(newCards: List[Card]) {
    _cards = newCards.reverse ::: _cards
  }
}
