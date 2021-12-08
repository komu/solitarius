package solitarius

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import solitarius.general.Tableau
import solitarius.rules.FreeCellTableau
import solitarius.rules.KlondikeTableau
import solitarius.rules.SpiderLevel
import solitarius.rules.SpiderTableau
import solitarius.ui.TableauView
import solitarius.ui.buildMenuBar
import javax.swing.*
import kotlin.system.exitProcess

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Solitarius") {
        var game by remember { mutableStateOf<Tableau>(KlondikeTableau()) }

        MenuBar {
            Menu("Game") {
                Menu("New Game") {
                    Item("FreeCell") { game = FreeCellTableau() }
                    Separator()
                    Item("Klondike") { game = KlondikeTableau() }
                    Separator()
                    Item("Spider - Easy") { game = SpiderTableau(SpiderLevel.Easy) }
                    Item("Spider - Medium") { game = SpiderTableau(SpiderLevel.Medium) }
                    Item("Spider - Hard") { game = SpiderTableau(SpiderLevel.Hard) }
                }
                Separator()

                Item("Quit") {
                    exitApplication()
                }
            }
        }

        MaterialTheme {
            TableauView(game)
        }
    }
}
