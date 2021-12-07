package solitarius

import solitarius.general.Tableau
import solitarius.rules.FreeCellTableau
import solitarius.rules.KlondikeTableau
import solitarius.rules.SpiderLevel
import solitarius.rules.SpiderTableau
import solitarius.ui.TableauView
import solitarius.ui.buildMenuBar
import java.awt.event.*
import javax.swing.*
import kotlin.system.exitProcess

class App {

    private val frame = JFrame("Solitarius")

    fun main() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        frame.jMenuBar = menuBar()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(600, 400)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    private fun newGame(tableau: Tableau) {
        frame.contentPane.removeAll()
        frame.contentPane.add(TableauView(tableau))
        frame.pack()
    }

    private fun menuBar() =
        buildMenuBar {
            submenu("Game") {
                submenu("New Game") {
                    action("FreeCell") { newGame(FreeCellTableau()) }
                    separator()
                    action("Klondike") { newGame(KlondikeTableau()) }
                    separator()
                    action("Spider - Easy") { newGame(SpiderTableau(SpiderLevel.Easy)) }
                    action("Spider - Medium") { newGame(SpiderTableau(SpiderLevel.Medium)) }
                    action("Spider - Hard") { newGame(SpiderTableau(SpiderLevel.Hard)) }
                }
                separator()
                action("Quit") { exitProcess(0) }
            }
            submenu("Help") {
                action("About Solitarius") { showAbout() }
            }
        }

    private fun showAbout() {
        val message =
            """
            Solitarius 0.4

            Copyright 2008-2021 Juha Komulainen
            """.trimIndent()

        JOptionPane.showMessageDialog(
            frame, message,
            "About Solitarius", JOptionPane.INFORMATION_MESSAGE
        )
    }
}

fun main() {
    App().main()
}
