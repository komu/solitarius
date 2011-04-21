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
package solitarius.ui

import java.awt.{ Image, Toolkit }
import java.io.FileNotFoundException

import solitarius.general.{ Card, Deck, Rank }

object CardImages {

  val cardWidth = 71
  val cardHeight = 96

  val backside = loadImage("/images/jiff/b1fv.png")
  
  val cardImages: Map[Card,Image] =
    Map((for (card <- Deck.cards) yield (card, loadImage(card))):_*)
    
  def loadImage(card: Card): Image = loadImage(imageFile(card).toString)
  
  def loadImage(file: String): Image = {
    val resource = getClass.getResource(file)
    if (resource != null)
      Toolkit.getDefaultToolkit.createImage(resource)
    else
      throw new FileNotFoundException(file)
  }
  
  def imageFile(card: Card): String = {
    val rank = if (card.rank == Rank.Ace) "1" else card.rank.shortName.toLowerCase
    String.format("/images/jiff/%s%s.png", 
                  card.suit.name.substring(0, 1).toLowerCase, rank)
  }
}
