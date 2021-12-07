package solitarius.ui

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JMenu
import javax.swing.JMenuBar

fun buildMenuBar(callback: MenuBarBuilder.() -> Unit): JMenuBar =
    MenuBarBuilder().apply(callback).menuBar

class MenuBarBuilder {
    val menuBar = JMenuBar()

    fun submenu(name: String, callback: MenuBuilder.() -> Unit) {
        val menu = JMenu(name)
        callback(MenuBuilder(menu))
        menuBar.add(menu)
    }
}

class MenuBuilder(private val menu: JMenu) {
    fun action(name: String, action: () -> Unit) {
        menu.add(object : AbstractAction(name) {
            override fun actionPerformed(e: ActionEvent) {
                action()
            }
        })
    }

    fun submenu(name: String, callback: MenuBuilder.() -> Unit) {
        val subMenu = JMenu(name)
        callback(MenuBuilder(subMenu))
        menu.add(subMenu)
    }

    fun separator() {
        menu.addSeparator()
    }
}
