package com.unciv.models.gamebasics.tech

import com.unciv.UnCivGame
import com.unciv.models.gamebasics.GameBasics
import com.unciv.models.gamebasics.ICivilopedia
import com.unciv.models.gamebasics.tr
import java.util.*

class Technology : ICivilopedia {

    lateinit var name: String

    var cost: Int = 0
    var prerequisites = HashSet<String>()
    var uniques = ArrayList<String>()

    var column: TechColumn? = null // The column that this tech is in the tech tree
    var row: Int = 0
    var quote=""

    override val description: String
        get() {
            val lineList = ArrayList<String>() // more readable than StringBuilder, with same performance for our use-case
            for (unique in uniques) lineList += unique.tr()

            val improvedImprovements = GameBasics.TileImprovements.values
                    .filter { it.improvingTech == name }.groupBy { it.improvingTechStats.toString() }
            for (improvement in improvedImprovements) {
                val impimpString = improvement.value.joinToString { it.name.tr() } +
                        " {provide" + (if (improvement.value.size == 1) "s" else "") + "} " + improvement.key
                lineList += impimpString.tr()
            }

            val currentPlayerCiv = UnCivGame.Current.gameInfo.currentPlayer
            var enabledUnits = GameBasics.Units.values.filter {
                it.requiredTech == name &&
                        (it.uniqueTo == null || it.uniqueTo == currentPlayerCiv)
            }
            val replacedUnits = enabledUnits.mapNotNull { it.replaces }
            enabledUnits = enabledUnits.filter { it.name !in replacedUnits }
            if (enabledUnits.isNotEmpty()) {
                lineList += "{Units enabled}: "
                for (unit in enabledUnits)
                    lineList += " * " + unit.name.tr() + " (" + unit.getShortDescription() + ")"
            }

            var enabledBuildings = GameBasics.Buildings.values.filter {
                it.requiredTech == name &&
                        (it.uniqueTo == null || it.uniqueTo == currentPlayerCiv)
            }
            val replacedBuildings = enabledBuildings.mapNotNull { it.replaces }
            enabledBuildings = enabledBuildings.filter { it.name !in replacedBuildings }
            val regularBuildings = enabledBuildings.filter { !it.isWonder && !it.isNationalWonder }
            if (regularBuildings.isNotEmpty()) {
                lineList += "{Buildings enabled}: "
                for (building in regularBuildings)
                    lineList += "* " + building.name.tr() + " (" + building.getShortDescription() + ")"
            }
            val wonders = enabledBuildings.filter { it.isWonder || it.isNationalWonder }
            if (wonders.isNotEmpty()) {
                lineList += "{Wonders enabled}: "
                for (wonder in wonders)
                    lineList += " * " + wonder.name.tr() + " (" + wonder.getShortDescription() + ")"
            }

            val revealedResource = GameBasics.TileResources.values.filter { it.revealedBy == name }.map { it.name }.firstOrNull() // can only be one
            if (revealedResource != null) lineList += "Reveals [$revealedResource] on the map".tr()

            val tileImprovements = GameBasics.TileImprovements.values.filter { it.techRequired == name }
            if (tileImprovements.isNotEmpty())
                lineList += "{Tile improvements enabled}: " + tileImprovements.joinToString { it.name.tr() }

            return lineList.joinToString("\n") { it.tr() }
        }

    override fun toString(): String {
        return name
    }

    fun era() = column!!.era
}
