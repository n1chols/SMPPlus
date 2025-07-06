package net.tob4n.lha

import net.tob4n.lha.features.HideNameTags
import net.tob4n.lha.features.OfflinePlayerBodies
import net.tob4n.lha.structures.Feature
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    private val features = mutableListOf<Feature>()

    override fun onEnable() {
        Feature.plugin = this
        features.add(HideNameTags())
        features.forEach { it.enable() }
    }

    override fun onDisable() {
        features.forEach { it.disable() }
    }
}
