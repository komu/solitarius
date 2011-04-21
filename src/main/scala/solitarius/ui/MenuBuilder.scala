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
package solitarius.ui

import javax.swing.{ AbstractAction, JMenu, JMenuBar }
import java.awt.event.ActionEvent

object MenuBarBuilder {
  def buildMenuBar(callback: MenuBarBuilder => Unit): JMenuBar = { 
    val builder = new MenuBarBuilder
    callback(builder)
    builder.menuBar
  }
}

class MenuBarBuilder {
  val menuBar = new JMenuBar
  
  def submenu(name: String) (callback: MenuBuilder => Unit): this.type = {
    val menu = new JMenu(name)
    callback(new MenuBuilder(menu))
    menuBar.add(menu)
    this
  }
}

class MenuBuilder(menu: JMenu) {
  def action(name: String) (action: => Unit) {
    menu.add(new AbstractAction(name) {
      override def actionPerformed(e: ActionEvent) = action
    })
  }
  
  def submenu(name: String) (callback: MenuBuilder => Unit): this.type = {
    val subMenu = new JMenu(name)
    callback(new MenuBuilder(subMenu))
    menu.add(subMenu)
    this
  }
    
  def separator() = menu.addSeparator()
}
