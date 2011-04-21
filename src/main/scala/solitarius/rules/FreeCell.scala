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

class FreeCellPile extends AlternateColorCascade {
  override def longestDraggableSequence = if (isEmpty) 0 else 1
  override def canDropOnEmpty(card: Card) = true
}

class FreeCellTableau extends Tableau(2, 8) {
  val cells       = Array.fill(4)(new Cell)
  val foundations = Array.fill(4)(new BySuitFoundation)
  val piles       = Array.fill(8)(new FreeCellPile)

  dealToPiles(piles, Deck.shuffledCards)

  def layout = cells :-: foundations :-: piles :-: end
}
