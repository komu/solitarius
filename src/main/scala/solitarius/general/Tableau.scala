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

abstract class Tableau(val rowCount: Int, val columnCount: Int) {

  lazy val table = {
    val table = Array.ofDim[Pile](rowCount, columnCount)
    for ((p, i) <- layout.piles.zipWithIndex) {
      table(i / columnCount)(i % columnCount) = p
    }
    table
  }
  
  def layout: Layout
  def empty: Pile = null
  def end = Layout(Nil)
    
  def rowHeights = table.map { row =>
    if (row.exists(p => p != null && p.showAsCascade)) 4 else 1
  }

  lazy val allPiles = (for {
    (row, y)  <- table.zipWithIndex
    (pile, x) <- row.zipWithIndex
    if (pile != null)
  } yield (x, y, pile)).toList
  
  def pileClicked(pile: Pile, count: Int) { 
  }
  
  case class Layout(piles: List[Pile]) {
    def :-: (p: Pile): Layout       = Layout(p :: piles)
    def :-: (ps: Seq[Pile]): Layout = Layout(ps.toList ::: piles)
  }
}
