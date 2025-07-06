package net.tob4n.smpplus.features

import com.destroystokyo.paper.MaterialTags
import io.papermc.paper.event.player.PlayerTrackEntityEvent
import io.papermc.paper.event.player.PlayerUntrackEntityEvent
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import net.tob4n.smpplus.structures.Feature
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class HideNameTags : Feature() {
    private val visibleEntitiesByPlayer = mutableMapOf<UUID, MutableMap<UUID, Boolean>>()

    override fun onEnable() {
        runTask(period = 5L) {
            for ((playerUUID, trackedEntities) in visibleEntitiesByPlayer) {
                val player = plugin.server.getPlayer(playerUUID) ?: continue
                for ((trackedEntityUUID, wasVisible) in trackedEntities) {
                    val trackedEntity = plugin.server.getEntity(trackedEntityUUID) ?: continue
                    val visible = player.canSeeName(trackedEntity)
                    if (visible == wasVisible) continue
                    trackedEntities[trackedEntityUUID] = visible
                    player.setNameVisibility(trackedEntity, visible)
                }
            }
        }
    }

    override fun onDisable() {
        visibleEntitiesByPlayer.clear()
    }

    @EventHandler
    fun onPlayerTrack(event: PlayerTrackEntityEvent) {
        val entity = event.entity
        val player = event.player
        if (!entity.hasNameTag) return
        visibleEntitiesByPlayer[player.uniqueId]?.set(entity.uniqueId, false)
        player.setNameVisibility(entity, false)
    }

    @EventHandler
    fun onPlayerUntrack(event: PlayerUntrackEntityEvent) {
        val entity = event.entity
        if (!entity.hasNameTag) return
        visibleEntitiesByPlayer[event.player.uniqueId]?.remove(entity.uniqueId)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        visibleEntitiesByPlayer[event.player.uniqueId] = mutableMapOf()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        visibleEntitiesByPlayer.remove(event.player.uniqueId)
    }

    private fun Player.canSeeName(target: Entity): Boolean {
        val eyeLocation = eyeLocation

        val nameLocation = if (target is Player) {
            target.eyeLocation.clone().add(0.0, 0.5, 0.0)
        } else {
            target.location.clone().add(0.0, target.height + 0.3, 0.0)
        }

        val direction = nameLocation.toVector().subtract(eyeLocation.toVector())

        val rayTrace = world.rayTraceBlocks(
            eyeLocation,
            direction,
            direction.length(),
            FluidCollisionMode.ALWAYS,
            true
        ) {
            block -> !block.type.isClear
        }

        return rayTrace == null
    }

    private fun Player.setNameVisibility(target: Entity, visible: Boolean) {
        val team = PlayerTeam(Scoreboard(), "${uniqueId}-${target.uniqueId}")

        val packet = if (visible) {
            ClientboundSetPlayerTeamPacket.createRemovePacket(team)
        } else {
            team.nameTagVisibility = Team.Visibility.NEVER
            team.players.add(target.name)
            ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true)
        }

        (this as CraftPlayer).handle.connection.send(packet)
    }

    private val Entity.hasNameTag: Boolean
        get() = this is Player || isCustomNameVisible

    private val Material.isClear: Boolean
        get() = this === Material.WATER ||
                this === Material.LADDER ||
                this === Material.BARRIER ||
                this === Material.IRON_BARS ||
                MaterialTags.GLASS.isTagged(this) ||
                MaterialTags.GLASS_PANES.isTagged(this) ||
                MaterialTags.FENCES.isTagged(this) ||
                MaterialTags.FENCE_GATES.isTagged(this)
}
