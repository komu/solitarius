package solitarius.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image
import solitarius.general.Card
import solitarius.general.Deck
import solitarius.general.Rank
import java.io.FileNotFoundException

object CardImages {

    val backside = loadImage("/images/jiff/b1fv.png")

    val cardImages: Map<Card, ImageBitmap> =
        Deck.cards.associateWith { loadImage(it) }

    private fun loadImage(card: Card): ImageBitmap = loadImage(imageFile(card))

    private fun loadImage(file: String): ImageBitmap {
        val resource = javaClass.getResource(file) ?: throw FileNotFoundException(file)

        return Image.makeFromEncoded(resource.readBytes()).toComposeImageBitmap()
    }

    private fun imageFile(card: Card): String {
        val rank = if (card.rank == Rank.Ace) "1" else card.rank.shortName.lowercase()
        return String.format("/images/jiff/%s%s.png", card.suit.name.substring(0, 1).lowercase(), rank)
    }
}
