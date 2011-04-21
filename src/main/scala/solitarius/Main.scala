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
package solitarius

import java.awt.event._
import javax.swing._

import general.Tableau
import rules.{ SpiderTableau, SpiderLevel, KlondikeTableau, FreeCellTableau }
import ui.TableauView
import ui._

object Main {
  
  val frame = new JFrame("Solitarius")
  
  def main(args: Array[String]) {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    frame.setJMenuBar(menuBar)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(600, 400)
    frame.setLocationRelativeTo(null)
    frame.setVisible(true)
  }
  
  def newGame(tableau: Tableau) {
    frame.getContentPane.removeAll()
    frame.getContentPane.add(new TableauView(tableau))
    frame.pack()
  }
  
  def menuBar =
    MenuBarBuilder.buildMenuBar { menuBar =>
      menuBar.submenu("Game") { game =>
        game.submenu("New Game") { sub =>
          sub.action("FreeCell") { newGame(new FreeCellTableau) }
          sub.separator
          sub.action("Klondike") { newGame(new KlondikeTableau) }
          sub.separator
          sub.action("Spider - Easy")   { newGame(new SpiderTableau(SpiderLevel.Easy)) }
          sub.action("Spider - Medium") { newGame(new SpiderTableau(SpiderLevel.Medium)) }
          sub.action("Spider - Hard")   { newGame(new SpiderTableau(SpiderLevel.Hard)) }
        }
        game.separator
        game.action("Quit") { System.exit(0) }
      }
      menuBar.submenu("Help") { help =>
        help.action("About Solitarius") { showAbout() }
      }
    }
      
  def showAbout() {
    val message =
        "Solitarius 0.3\n" +
        "\n" +
        "Copyright 2008 Juha Komulainen\n"

    JOptionPane.showMessageDialog(
        frame, message, 
        "About Solitarius", JOptionPane.INFORMATION_MESSAGE)
  }
}