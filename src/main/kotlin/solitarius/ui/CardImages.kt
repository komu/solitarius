package solitarius.ui

import solitarius.general.Card
import solitarius.general.Deck
import solitarius.general.Rank
import java.awt.Image
import java.awt.Toolkit
import java.io.FileNotFoundException

object CardImages {

    const val cardWidth = 71
    const val cardHeight = 96

    val backside = loadImage("/images/jiff/b1fv.png")

    val cardImages: Map<Card, Image> =
        Deck.cards.associateWith { loadImage(it) }

    private fun loadImage(card: Card): Image = loadImage(imageFile(card))

    private fun loadImage(file: String): Image {
        val resource = javaClass.getResource(file) ?: throw FileNotFoundException(file)
        return Toolkit.getDefaultToolkit().createImage(resource)
    }

    private fun imageFile(card: Card): String {
        val rank = if (card.rank == Rank.Ace) "1" else card.rank.shortName.lowercase()
        return String.format("/images/jiff/%s%s.png", card.suit.name.substring(0, 1).lowercase(), rank)
    }
}
