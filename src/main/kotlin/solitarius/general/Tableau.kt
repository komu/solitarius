package solitarius.general

abstract class Tableau(val rowCount: Int, val columnCount: Int) {

    private val table: Array<Array<Pile?>> by lazy {
        val table = Array<Array<Pile?>>(rowCount) { Array(columnCount) { null } }

        for ((i, p) in buildLayout().withIndex())
            table[i / columnCount][i % columnCount] = p
        table
    }

    protected abstract fun buildLayout(): List<Pile?>
    val empty: Pile? = null
    val end = Layout(emptyList())

    val rowHeights: List<Int>
        get() = table.map { row ->
            if (row.any { p -> p != null && p.showAsCascade }) 4 else 1
        }

    val allPiles: List<Triple<Int, Int, Pile>> by lazy {
        buildList {
            for ((y, row) in table.withIndex())
                for ((x, pile) in row.withIndex())
                    if (pile != null)
                        add(Triple(x, y, pile))
        }
    }

    open fun pileClicked(pile: Pile, count: Int) {
    }

    class Layout(val piles: List<Pile?>) {

        constructor(pile: Pile): this(listOf(pile))

        infix fun beside(p: Pile?) = Layout(piles + p)
        infix fun beside(ps: List<Pile>) = Layout(piles + ps)
        infix fun beside(layout: Layout) = Layout(piles + layout.piles)
    }
}
